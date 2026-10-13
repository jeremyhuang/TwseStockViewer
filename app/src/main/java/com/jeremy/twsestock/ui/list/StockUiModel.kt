package com.jeremy.twsestock.ui.list

import com.jeremy.twsestock.data.model.PriceTrend
import java.math.BigDecimal

/**
 * 股票卡片的顯示用資料。
 *
 * 數值保留原始型別，格式化交由 ui/common/StockFormatExt.kt 的擴充函式在綁定時處理。
 * 數值為 null 代表證交所沒有提供該欄位（例如當日無成交），畫面會顯示「-」。
 */
data class StockUiModel(
    /** 股票代號，例如 "2330"。 */
    val code: String,
    /** 股票名稱，例如 "台積電"。 */
    val name: String,
    /** 收盤價（元）。 */
    val closingPrice: BigDecimal?,
    /** 收盤價與月平均價比較的結果，決定收盤價與月均標籤的顏色。 */
    val closingTrend: PriceTrend,
    /** 漲跌價差（元），正數為漲、負數為跌。 */
    val change: BigDecimal?,
    /** 漲跌幅（%），正數為漲、負數為跌。 */
    val changePercent: BigDecimal?,
    /** 漲跌價差的正負，決定漲跌文字的顏色。 */
    val changeTrend: PriceTrend,
    /** 月平均價（元）。 */
    val monthlyAveragePrice: BigDecimal?,
    /** 開盤價（元）。 */
    val openingPrice: BigDecimal?,
    /** 最高價（元）。 */
    val highestPrice: BigDecimal?,
    /** 最低價（元）。 */
    val lowestPrice: BigDecimal?,
    /** 成交股數（股），顯示時換算成張（1 張 = 1,000 股）。 */
    val tradeVolume: Long?,
    /** 成交筆數。 */
    val transactionCount: Long?,
    /** 成交金額（元），顯示時換算成億元。 */
    val tradeValue: Long?,
)
