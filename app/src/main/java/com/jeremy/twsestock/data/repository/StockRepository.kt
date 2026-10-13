package com.jeremy.twsestock.data.repository

import com.jeremy.twsestock.data.model.MarketSnapshot
import com.jeremy.twsestock.data.model.Stock
import java.time.LocalDate

/**
 * 上市個股資料的來源。ViewModel 只透過這個類別取得資料，不需要知道資料從哪裡來。
 *
 * 目前回傳固定的假資料，用來先完成畫面；之後改成呼叫證交所 API 時只需要修改這個類別。
 */
class StockRepository {

    // TODO: 改成並行呼叫 STOCK_DAY_ALL、STOCK_DAY_AVG_ALL、BWIBBU_ALL，並依股票代號合併。
    /**
     * 取得最新交易日的所有上市個股資料。
     *
     * @return 交易日期與所有個股。
     * @throws Exception 網路或資料解析失敗時拋出。
     */
    suspend fun getMarketSnapshot(): MarketSnapshot = MarketSnapshot(
        tradeDate = LocalDate.of(2026, 10, 8),
        stocks = listOf(
            Stock(
                code = "2330", name = "台積電",
                openingPrice = "1045".toBigDecimal(), highestPrice = "1060".toBigDecimal(),
                lowestPrice = "1040".toBigDecimal(), closingPrice = "1055".toBigDecimal(),
                change = "15".toBigDecimal(), monthlyAveragePrice = "1032.5".toBigDecimal(),
                tradeVolume = 32_451_203, tradeValue = 34_236_000_000, transactionCount = 45_210,
                peRatio = "25.12".toBigDecimal(), dividendYield = "1.52".toBigDecimal(), pbRatio = "7.45".toBigDecimal(),
            ),
            Stock(
                code = "2317", name = "鴻海",
                openingPrice = "201".toBigDecimal(), highestPrice = "202".toBigDecimal(),
                lowestPrice = "197.5".toBigDecimal(), closingPrice = "198.5".toBigDecimal(),
                change = "-2.5".toBigDecimal(), monthlyAveragePrice = "203.2".toBigDecimal(),
                tradeVolume = 41_820_500, tradeValue = 8_301_000_000, transactionCount = 28_440,
                peRatio = "14.30".toBigDecimal(), dividendYield = "2.66".toBigDecimal(), pbRatio = "1.85".toBigDecimal(),
            ),
            // 今天上漲但仍低於月均價：兩組顏色不同。
            Stock(
                code = "2882", name = "國泰金",
                openingPrice = "66.3".toBigDecimal(), highestPrice = "67".toBigDecimal(),
                lowestPrice = "66.2".toBigDecimal(), closingPrice = "66.8".toBigDecimal(),
                change = "0.6".toBigDecimal(), monthlyAveragePrice = "67.9".toBigDecimal(),
                tradeVolume = 22_105_000, tradeValue = 1_476_000_000, transactionCount = 11_240,
                peRatio = "9.87".toBigDecimal(), dividendYield = "4.49".toBigDecimal(), pbRatio = "1.32".toBigDecimal(),
            ),
            Stock(
                code = "2603", name = "長榮",
                openingPrice = "201".toBigDecimal(), highestPrice = "206".toBigDecimal(),
                lowestPrice = "200.5".toBigDecimal(), closingPrice = "205".toBigDecimal(),
                change = "4.5".toBigDecimal(), monthlyAveragePrice = "198".toBigDecimal(),
                tradeVolume = 18_600_400, tradeValue = 3_813_000_000, transactionCount = 15_320,
                peRatio = "4.21".toBigDecimal(), dividendYield = "8.78".toBigDecimal(), pbRatio = "0.98".toBigDecimal(),
            ),
            // 平盤，且收盤價等於月均價。
            Stock(
                code = "2412", name = "中華電",
                openingPrice = "128.5".toBigDecimal(), highestPrice = "129".toBigDecimal(),
                lowestPrice = "128".toBigDecimal(), closingPrice = "128.5".toBigDecimal(),
                change = "0".toBigDecimal(), monthlyAveragePrice = "128.5".toBigDecimal(),
                tradeVolume = 6_203_100, tradeValue = 797_000_000, transactionCount = 4_120,
                peRatio = "26.33".toBigDecimal(), dividendYield = "3.73".toBigDecimal(), pbRatio = "2.58".toBigDecimal(),
            ),
            // 今天下跌但仍高於月均價。
            Stock(
                code = "2454", name = "聯發科",
                openingPrice = "1330".toBigDecimal(), highestPrice = "1335".toBigDecimal(),
                lowestPrice = "1315".toBigDecimal(), closingPrice = "1320".toBigDecimal(),
                change = "-10".toBigDecimal(), monthlyAveragePrice = "1285".toBigDecimal(),
                tradeVolume = 5_210_300, tradeValue = 6_877_000_000, transactionCount = 9_870,
                peRatio = "19.64".toBigDecimal(), dividendYield = "4.17".toBigDecimal(), pbRatio = "5.42".toBigDecimal(),
            ),
            // 虧損公司沒有本益比，API 回傳空字串，這裡以 null 表示。
            Stock(
                code = "1101", name = "台泥",
                openingPrice = "32.15".toBigDecimal(), highestPrice = "32.4".toBigDecimal(),
                lowestPrice = "32".toBigDecimal(), closingPrice = "32.2".toBigDecimal(),
                change = "0.05".toBigDecimal(), monthlyAveragePrice = "32.6".toBigDecimal(),
                tradeVolume = 9_802_000, tradeValue = 315_600_000, transactionCount = 5_210,
                peRatio = null, dividendYield = "3.11".toBigDecimal(), pbRatio = "0.83".toBigDecimal(),
            ),
        ),
    )
}
