package com.jeremy.twsestock.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** `/exchangeReport/STOCK_DAY_AVG_ALL` 的單筆資料：上市個股日收盤價及月平均價。 */
@JsonClass(generateAdapter = true)
data class StockDayAvgDto(
    /** 交易日期，民國年格式，例如 "1151008"。 */
    @Json(name = "Date") val date: String? = null,
    /** 證券代號。 */
    @Json(name = "Code") val code: String? = null,
    /** 證券名稱。 */
    @Json(name = "Name") val name: String? = null,
    /** 收盤價。 */
    @Json(name = "ClosingPrice") val closingPrice: String? = null,
    /** 月平均價。 */
    @Json(name = "MonthlyAveragePrice") val monthlyAveragePrice: String? = null,
)
