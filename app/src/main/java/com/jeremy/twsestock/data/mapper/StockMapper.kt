package com.jeremy.twsestock.data.mapper

import com.jeremy.twsestock.common.toRocDate
import com.jeremy.twsestock.common.toTwseDecimal
import com.jeremy.twsestock.common.toTwseLong
import com.jeremy.twsestock.data.model.MarketSnapshot
import com.jeremy.twsestock.data.model.Stock
import com.jeremy.twsestock.data.remote.dto.StockDayAvgDto
import com.jeremy.twsestock.data.remote.dto.StockDayDto
import com.jeremy.twsestock.data.remote.dto.ValuationDto
import java.time.LocalDateTime

/**
 * 將證交所三支 API 的資料依股票代號合併成 [Stock]。
 *
 * 以 STOCK_DAY_ALL 為主表（left join）：另外兩支 API 涵蓋的證券較少，
 * 例如 ETF、權證沒有本益比，找不到對應代號時該欄位為 null。
 */
class StockMapper {

    /**
     * 合併三支 API 的回應。
     *
     * @param dailyTrading STOCK_DAY_ALL 的資料，決定列表包含哪些證券。
     * @param monthlyAverages STOCK_DAY_AVG_ALL 的資料，提供月平均價。
     * @param valuations BWIBBU_ALL 的資料，提供本益比、殖利率、股價淨值比。
     * @param fetchedAt App 取得這批資料的時間。
     * @return 交易日期與合併後的所有證券；代號為空的資料會被略過。
     */
    fun map(
        dailyTrading: List<StockDayDto>,
        monthlyAverages: List<StockDayAvgDto>,
        valuations: List<ValuationDto>,
        fetchedAt: LocalDateTime,
    ): MarketSnapshot {
        val averagesByCode = monthlyAverages.associateByCode { it.code }
        val valuationsByCode = valuations.associateByCode { it.code }

        val stocks = dailyTrading.mapNotNull { daily ->
            val code = daily.code?.trim()
            if (code.isNullOrEmpty()) return@mapNotNull null
            val average = averagesByCode[code]
            val valuation = valuationsByCode[code]

            Stock(
                code = code,
                name = daily.name?.trim().orEmpty(),
                openingPrice = daily.openingPrice.toTwseDecimal(),
                highestPrice = daily.highestPrice.toTwseDecimal(),
                lowestPrice = daily.lowestPrice.toTwseDecimal(),
                closingPrice = daily.closingPrice.toTwseDecimal(),
                change = daily.change.toTwseDecimal(),
                monthlyAveragePrice = average?.monthlyAveragePrice.toTwseDecimal(),
                tradeVolume = daily.tradeVolume.toTwseLong(),
                tradeValue = daily.tradeValue.toTwseLong(),
                transactionCount = daily.transaction.toTwseLong(),
                peRatio = valuation?.peRatio.toTwseDecimal(),
                dividendYield = valuation?.dividendYield.toTwseDecimal(),
                pbRatio = valuation?.pbRatio.toTwseDecimal(),
            )
        }

        val tradeDate = dailyTrading.firstNotNullOfOrNull { it.date.toRocDate() }
        return MarketSnapshot(tradeDate = tradeDate, fetchedAt = fetchedAt, stocks = stocks)
    }

    /**
     * 以股票代號建立查詢表，略過代號為空的資料。
     *
     * @param code 取出代號的方式。
     * @return 代號對應資料的 Map。
     */
    private inline fun <T> List<T>.associateByCode(code: (T) -> String?): Map<String, T> =
        mapNotNull { item -> code(item)?.trim()?.takeIf(String::isNotEmpty)?.let { it to item } }.toMap()
}
