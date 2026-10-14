package com.jeremy.twsestock.data.remote

import com.jeremy.twsestock.common.toRocDate
import com.jeremy.twsestock.common.toTwseDecimal
import com.jeremy.twsestock.data.mapper.StockMapper
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeNoException
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 實際呼叫證交所 OpenAPI，確認：
 * 1. API 連得到，回應 HTTP 200
 * 2. API 回傳的欄位名稱跟 DTO 的 @Json 名稱相符（證交所改欄位時這裡會先失敗）
 * 3. 回傳的資料格式符合預期：有資料、代號不為空、日期是民國年、數值都解析得出來
 *
 * 需要網路；連不到證交所時（例如離線）會略過，不算失敗。
 * 資料每天變動，所以只檢查格式與結構，不檢查固定數值。
 */
class TwseApiTest {

    private val moshi = Moshi.Builder().build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: TwseApi = Retrofit.Builder()
        .baseUrl(TwseApi.BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(TwseApi::class.java)

    /**
     * 直接用 OkHttp 抓原始 JSON，檢查 HTTP 狀態碼，回傳第一筆資料的欄位名稱。
     * 連不到網路時讓測試變成「略過」。
     *
     * @param path API 路徑，例如 exchangeReport/STOCK_DAY_ALL。
     * @return 第一筆資料的所有欄位名稱。
     */
    private fun fetchFieldNames(path: String): Set<String> {
        val request = Request.Builder().url(TwseApi.BASE_URL + path).build()
        val response = try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            println("無法連線到證交所，略過測試：${e.message}")
            assumeNoException(e)
            throw e
        }
        response.use {
            println("HTTP 狀態碼：期待 200，實際 ${it.code}")
            assertEquals(200, it.code)

            val body = it.body.string()
            val type = Types.newParameterizedType(List::class.java, Map::class.java)
            val rows = moshi.adapter<List<Map<String, Any?>>>(type).fromJson(body).orEmpty()
            println("回傳筆數：${rows.size}")
            assertTrue("回傳的資料不應為空", rows.isNotEmpty())
            return rows.first().keys
        }
    }

    /**
     * 比對 API 實際回傳的欄位與 DTO 需要的欄位。
     *
     * @param actual API 實際回傳的欄位。
     * @param expected DTO 的 @Json 名稱。
     */
    private fun assertFieldsMatch(actual: Set<String>, expected: Set<String>) {
        println("DTO 需要的欄位：$expected")
        println("API 實際的欄位：$actual")
        val missing = expected - actual
        println("API 缺少的欄位：期待 []，實際 $missing")
        assertTrue("API 缺少 DTO 需要的欄位：$missing", missing.isEmpty())
        val extra = actual - expected
        if (extra.isNotEmpty()) println("（API 多了 DTO 沒用到的欄位，不影響：$extra）")
    }

    /**
     * 檢查一個數值欄位：非空的值都要能解析成數字。
     *
     * @param name 欄位名稱。
     * @param values 所有資料的這個欄位值。
     */
    private fun assertNumbersParsable(name: String, values: List<String?>) {
        val invalid = values.filter { !it.isNullOrBlank() && it.toTwseDecimal() == null && it.trim() !in PLACEHOLDERS }
        println("$name 無法解析的值：期待 0 筆，實際 ${invalid.size} 筆 ${invalid.take(5)}")
        assertTrue("$name 有無法解析的值：${invalid.take(5)}", invalid.isEmpty())
    }

    /**
     * STOCK_DAY_ALL：連線、欄位、資料格式。
     *
     * 測試對象：[TwseApi.getDailyTrading]、[com.jeremy.twsestock.data.remote.dto.StockDayDto]
     */
    @Test
    fun stockDayAll_isReachableAndMatchesDto() = runBlocking {
        val fields = fetchFieldNames("exchangeReport/STOCK_DAY_ALL")
        assertFieldsMatch(
            fields,
            setOf(
                "Date", "Code", "Name", "TradeVolume", "TradeValue", "OpeningPrice",
                "HighestPrice", "LowestPrice", "ClosingPrice", "Change", "Transaction",
            ),
        )

        val data = api.getDailyTrading()
        println("第一筆資料：${data.first()}")

        val blankCodes = data.count { it.code.isNullOrBlank() }
        println("代號為空：期待 0 筆，實際 $blankCodes 筆")
        assertEquals(0, blankCodes)

        val date = data.first().date
        println("日期 \"$date\" 轉西元：期待能轉換，實際 ${date.toRocDate()}")
        assertTrue("日期不是民國年格式：$date", date.toRocDate() != null)

        assertNumbersParsable("ClosingPrice", data.map { it.closingPrice })
        assertNumbersParsable("Change", data.map { it.change })
        assertNumbersParsable("TradeVolume", data.map { it.tradeVolume })
    }

    /**
     * STOCK_DAY_AVG_ALL：連線、欄位、資料格式。
     *
     * 測試對象：[TwseApi.getMonthlyAverages]、[com.jeremy.twsestock.data.remote.dto.StockDayAvgDto]
     */
    @Test
    fun stockDayAvgAll_isReachableAndMatchesDto() = runBlocking {
        val fields = fetchFieldNames("exchangeReport/STOCK_DAY_AVG_ALL")
        assertFieldsMatch(fields, setOf("Date", "Code", "Name", "ClosingPrice", "MonthlyAveragePrice"))

        val data = api.getMonthlyAverages()
        println("第一筆資料：${data.first()}")

        val blankCodes = data.count { it.code.isNullOrBlank() }
        println("代號為空：期待 0 筆，實際 $blankCodes 筆")
        assertEquals(0, blankCodes)

        assertNumbersParsable("MonthlyAveragePrice", data.map { it.monthlyAveragePrice })
    }

    /**
     * BWIBBU_ALL：連線、欄位、資料格式。
     *
     * 測試對象：[TwseApi.getValuations]、[com.jeremy.twsestock.data.remote.dto.ValuationDto]
     */
    @Test
    fun bwibbuAll_isReachableAndMatchesDto() = runBlocking {
        val fields = fetchFieldNames("exchangeReport/BWIBBU_ALL")
        assertFieldsMatch(fields, setOf("Date", "Code", "Name", "PEratio", "DividendYield", "PBratio"))

        val data = api.getValuations()
        println("第一筆資料：${data.first()}")

        val blankCodes = data.count { it.code.isNullOrBlank() }
        println("代號為空：期待 0 筆，實際 $blankCodes 筆")
        assertEquals(0, blankCodes)

        assertNumbersParsable("PEratio", data.map { it.peRatio })
        assertNumbersParsable("DividendYield", data.map { it.dividendYield })
        assertNumbersParsable("PBratio", data.map { it.pbRatio })
    }

    /**
     * 三支 API 的資料能依代號合併成畫面要用的 Stock。
     *
     * 測試對象：[TwseApi] 三支 API + [StockMapper.map]
     */
    @Test
    fun allApis_mergeIntoStocks() = runBlocking {
        fetchFieldNames("exchangeReport/STOCK_DAY_ALL") // 順便確認連得到，連不到就略過

        val daily = api.getDailyTrading()
        val snapshot = StockMapper().map(daily, api.getMonthlyAverages(), api.getValuations(), LocalDateTime.now())

        println("合併後檔數：期待等於 STOCK_DAY_ALL 的 ${daily.size}，實際 ${snapshot.stocks.size}")
        assertEquals(daily.size, snapshot.stocks.size)
        println("交易日期：${snapshot.tradeDate}")
        assertTrue("交易日期應能解析", snapshot.tradeDate != null)

        val withAverage = snapshot.stocks.count { it.monthlyAveragePrice != null }
        val withPe = snapshot.stocks.count { it.peRatio != null }
        println("有月均價：$withAverage 檔；有本益比：$withPe 檔（ETF、虧損公司沒有本益比）")
        assertTrue("至少要有一檔對得到月均價，代號對不上時會是 0", withAverage > 0)
        assertTrue("至少要有一檔對得到本益比，代號對不上時會是 0", withPe > 0)

        val tsmc = snapshot.stocks.firstOrNull { it.code == "2330" }
        println("台積電：$tsmc")
        assertTrue("應該找得到台積電（2330）", tsmc != null)
    }

    private companion object {
        /** 證交所用來表示「沒有資料」的值，不算解析失敗。 */
        val PLACEHOLDERS = setOf("-", "--", "---", "N/A")
    }
}
