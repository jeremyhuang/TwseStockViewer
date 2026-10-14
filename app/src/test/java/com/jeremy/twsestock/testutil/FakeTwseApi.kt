package com.jeremy.twsestock.testutil

import com.jeremy.twsestock.data.remote.TwseApi
import com.jeremy.twsestock.data.remote.dto.StockDayAvgDto
import com.jeremy.twsestock.data.remote.dto.StockDayDto
import com.jeremy.twsestock.data.remote.dto.ValuationDto

/**
 * 測試用的證交所 API，回傳固定資料，不連網路。
 *
 * @property error 設定後，每支 API 都會拋出這個例外，用來測試載入失敗。
 */
class FakeTwseApi(
    private val dailyTrading: List<StockDayDto> = TestStocks.dailyTrading,
    private val monthlyAverages: List<StockDayAvgDto> = TestStocks.monthlyAverages,
    private val valuations: List<ValuationDto> = TestStocks.valuations,
    var error: Throwable? = null,
) : TwseApi {

    override suspend fun getDailyTrading(): List<StockDayDto> = error?.let { throw it } ?: dailyTrading

    override suspend fun getMonthlyAverages(): List<StockDayAvgDto> = error?.let { throw it } ?: monthlyAverages

    override suspend fun getValuations(): List<ValuationDto> = error?.let { throw it } ?: valuations
}

/**
 * 測試用的三筆資料：
 * - 2330 台積電：上漲、收盤高於月均，成交量第二
 * - 2317 鴻海：下跌、收盤低於月均，成交量最大
 * - 0050 元大台灣50：ETF、上漲、收盤高於月均，成交量最小
 */
object TestStocks {

    val dailyTrading = listOf(
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
    )

    val monthlyAverages = listOf(
        StockDayAvgDto(code = "2330", monthlyAveragePrice = "1032.50"),
        StockDayAvgDto(code = "2317", monthlyAveragePrice = "203.20"),
        StockDayAvgDto(code = "0050", monthlyAveragePrice = "176.40"),
    )

    val valuations = listOf(
        ValuationDto(code = "2330", peRatio = "25.12", dividendYield = "1.52", pbRatio = "7.45"),
        ValuationDto(code = "2317", peRatio = "14.30", dividendYield = "2.66", pbRatio = "1.85"),
    )
}
