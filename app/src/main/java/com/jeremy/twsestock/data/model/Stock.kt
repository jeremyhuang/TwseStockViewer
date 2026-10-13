package com.jeremy.twsestock.data.model

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * 單一上市個股某個交易日的資料
 */
data class Stock(
    val code: String,                       // 股票代號
    val name: String,                       // 股票名稱
    val openingPrice: BigDecimal?,          // 開盤價
    val highestPrice: BigDecimal?,          // 最高價
    val lowestPrice: BigDecimal?,           // 最低價
    val closingPrice: BigDecimal?,          // 收盤價
    val change: BigDecimal?,                // 漲跌價差
    val monthlyAveragePrice: BigDecimal?,   // 月平均價
    val tradeVolume: Long?,                 // 成交股數
    val tradeValue: Long?,                  // 成交金額
    val transactionCount: Long?,            // 成交筆數
    val peRatio: BigDecimal?,               // 本益比
    val dividendYield: BigDecimal?,         // 殖利率
    val pbRatio: BigDecimal?,               // 股價淨值比
) {
    /** 收盤價與月平均價比較的結果 */
    val closingVsMonthlyAverage: PriceTrend
        get() = PriceTrend.of(closingPrice, monthlyAveragePrice)

    /** 漲跌價差的方向 */
    val changeTrend: PriceTrend
        get() = PriceTrend.of(change, BigDecimal.ZERO)

    /**
     * 漲跌幅，四捨五入到小數兩位。
     * 以「昨收 = 收盤價 − 漲跌價差」為基準計算；收盤價或漲跌價差缺少、或昨收為 0 時為 null。
     */
    val changePercent: BigDecimal?
        get() {
            val close = closingPrice ?: return null
            val diff = change ?: return null
            val previousClose = close - diff
            if (previousClose.signum() == 0) return null
            return diff.divide(previousClose, MathContext.DECIMAL64)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP)
        }

    private companion object {
        val ONE_HUNDRED = BigDecimal(100)
    }
}
