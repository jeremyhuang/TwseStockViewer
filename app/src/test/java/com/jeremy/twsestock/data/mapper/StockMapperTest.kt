package com.jeremy.twsestock.data.mapper

import com.jeremy.twsestock.data.model.PriceTrend
import com.jeremy.twsestock.data.remote.dto.StockDayAvgDto
import com.jeremy.twsestock.data.remote.dto.StockDayDto
import com.jeremy.twsestock.data.remote.dto.ValuationDto
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class StockMapperTest {

    private val mapper = StockMapper()
    private val fetchedAt = LocalDateTime.of(2026, 10, 13, 17, 20, 35)

    private val tsmcDaily = StockDayDto(
        date = "1151008", code = "2330", name = "台積電",
        tradeVolume = "32451203", tradeValue = "34236000000",
        openingPrice = "1045.00", highestPrice = "1060.00", lowestPrice = "1040.00",
        closingPrice = "1055.00", change = "15.0000", transaction = "45210",
    )

    /**
     * 三支 API 依代號合併成一筆 Stock。
     *
     * 測試對象：[StockMapper.map]
     */
    @Test
    fun map_sameCode_mergesThreeApisIntoOneStock() {
        val snapshot = mapper.map(
            dailyTrading = listOf(tsmcDaily),
            monthlyAverages = listOf(StockDayAvgDto(code = "2330", monthlyAveragePrice = "1032.50")),
            valuations = listOf(ValuationDto(code = "2330", peRatio = "25.12", dividendYield = "1.52", pbRatio = "7.45")),
            fetchedAt = fetchedAt,
        )
        val stock = snapshot.stocks.single()

        println("交易日期：期待 2026-10-08，實際 ${snapshot.tradeDate}")
        assertEquals(LocalDate.of(2026, 10, 8), snapshot.tradeDate)
        println("名稱：期待 台積電，實際 ${stock.name}")
        assertEquals("台積電", stock.name)
        println("收盤價（STOCK_DAY_ALL）：期待 1055.00，實際 ${stock.closingPrice}")
        assertEquals("1055.00".toBigDecimal(), stock.closingPrice)
        println("成交股數（STOCK_DAY_ALL）：期待 32451203，實際 ${stock.tradeVolume}")
        assertEquals(32_451_203L, stock.tradeVolume)
        println("月均價（STOCK_DAY_AVG_ALL）：期待 1032.50，實際 ${stock.monthlyAveragePrice}")
        assertEquals("1032.50".toBigDecimal(), stock.monthlyAveragePrice)
        println("本益比（BWIBBU_ALL）：期待 25.12，實際 ${stock.peRatio}")
        assertEquals("25.12".toBigDecimal(), stock.peRatio)
        println("收盤 vs 月均：期待 UP，實際 ${stock.closingVsMonthlyAverage}")
        assertEquals(PriceTrend.UP, stock.closingVsMonthlyAverage)
        println("漲跌方向：期待 UP，實際 ${stock.changeTrend}")
        assertEquals(PriceTrend.UP, stock.changeTrend)
    }

    /**
     * 其他兩支 API 沒有該代號時，對應欄位為 null（left join）。
     *
     * 測試對象：[StockMapper.map]
     */
    @Test
    fun map_codeMissingInOtherApis_leavesFieldsNull() {
        val etf = StockDayDto(code = "0050", name = "元大台灣50", closingPrice = "180.00", change = "0.0000")

        val stock = mapper.map(listOf(etf), emptyList(), emptyList(), fetchedAt).stocks.single()

        println("月均價：期待 null，實際 ${stock.monthlyAveragePrice}")
        assertEquals(null, stock.monthlyAveragePrice)
        println("本益比：期待 null，實際 ${stock.peRatio}")
        assertEquals(null, stock.peRatio)
        println("收盤 vs 月均：期待 FLAT，實際 ${stock.closingVsMonthlyAverage}")
        assertEquals(PriceTrend.FLAT, stock.closingVsMonthlyAverage)
    }

    /**
     * 空字串欄位轉成 null，不影響其他欄位。
     *
     * 測試對象：[StockMapper.map]
     */
    @Test
    fun map_emptyStringFields_becomeNullWithoutAffectingOthers() {
        val noTrade = StockDayDto(code = "00625K", name = "富邦上証+R", closingPrice = "", tradeVolume = "", change = "0.0000")
        val noPe = ValuationDto(code = "00625K", peRatio = "", dividendYield = "3.11")

        val stock = mapper.map(listOf(noTrade), emptyList(), listOf(noPe), fetchedAt).stocks.single()

        println("收盤價 \"\"：期待 null，實際 ${stock.closingPrice}")
        assertEquals(null, stock.closingPrice)
        println("成交股數 \"\"：期待 null，實際 ${stock.tradeVolume}")
        assertEquals(null, stock.tradeVolume)
        println("本益比 \"\"：期待 null，實際 ${stock.peRatio}")
        assertEquals(null, stock.peRatio)
        println("殖利率 \"3.11\"：期待 3.11，實際 ${stock.dividendYield}")
        assertEquals("3.11".toBigDecimal(), stock.dividendYield)
    }
}
