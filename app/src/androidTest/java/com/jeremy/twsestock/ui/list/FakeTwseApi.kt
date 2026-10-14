package com.jeremy.twsestock.ui.list

import com.jeremy.twsestock.data.remote.TwseApi
import com.jeremy.twsestock.data.remote.dto.StockDayAvgDto
import com.jeremy.twsestock.data.remote.dto.StockDayDto
import com.jeremy.twsestock.data.remote.dto.ValuationDto

/**
 * UI 測試用的證交所 API，回傳固定的三筆資料，不連網路：
 * - 2330 台積電：上漲
 * - 2317 鴻海：下跌
 * - 0050 元大台灣50：ETF、上漲
 *
 * @param fillerCount 額外加入的填充股票數量，用來讓列表長到可以捲動。
 * 填充股票的成交量最小，預設排序下一定排在上面三檔之後，不影響其他測試。
 * @property error 設定後每支 API 都拋出這個例外，用來模擬斷網；設回 null 即恢復。
 * @property addedStocks 額外追加的股票，用來模擬重新整理後拿到新資料。
 */
class FakeTwseApi(
    private val fillerCount: Int = 0,
    @Volatile var error: Throwable? = null,
) : TwseApi {

    @Volatile
    var addedStocks: List<StockDayDto> = emptyList()

    override suspend fun getDailyTrading(): List<StockDayDto> = error?.let { throw it } ?: (listOf(
        StockDayDto(
            date = "1151008", code = "2330", name = "台積電",
            tradeVolume = "32451203", tradeValue = "34236000000",
            openingPrice = "1045.00", highestPrice = "1060.00", lowestPrice = "1040.00",
            closingPrice = "1055.00", change = "15.0000", transaction = "45210",
        ),
        StockDayDto(
            date = "1151008", code = "2317", name = "鴻海",
            tradeVolume = "41820500", tradeValue = "8301000000",
            openingPrice = "201.00", highestPrice = "202.00", lowestPrice = "197.50",
            closingPrice = "198.50", change = "-2.5000", transaction = "28440",
        ),
        StockDayDto(
            date = "1151008", code = "0050", name = "元大台灣50",
            tradeVolume = "10000000", tradeValue = "1802500000",
            openingPrice = "179.00", highestPrice = "180.50", lowestPrice = "178.80",
            closingPrice = "180.25", change = "1.3500", transaction = "12000",
        ),
    ) + List(fillerCount) { index ->
        StockDayDto(
            date = "1151008", code = (9100 + index).toString(), name = "填充股票${index + 1}",
            tradeVolume = "1000", tradeValue = "10000",
            openingPrice = "10.00", highestPrice = "10.00", lowestPrice = "10.00",
            closingPrice = "10.00", change = "0.0000", transaction = "1",
        )
    } + addedStocks)

    override suspend fun getMonthlyAverages(): List<StockDayAvgDto> = error?.let { throw it } ?: listOf(
        StockDayAvgDto(code = "2330", monthlyAveragePrice = "1032.50"),
        StockDayAvgDto(code = "2317", monthlyAveragePrice = "203.20"),
        StockDayAvgDto(code = "0050", monthlyAveragePrice = "176.40"),
    )

    override suspend fun getValuations(): List<ValuationDto> = error?.let { throw it } ?: listOf(
        ValuationDto(code = "2330", peRatio = "25.12", dividendYield = "1.52", pbRatio = "7.45"),
        ValuationDto(code = "2317", peRatio = "14.30", dividendYield = "2.66", pbRatio = "1.85"),
    )
}
