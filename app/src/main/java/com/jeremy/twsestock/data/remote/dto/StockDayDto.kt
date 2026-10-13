package com.jeremy.twsestock.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * `/exchangeReport/STOCK_DAY_ALL` 的單筆資料：上市個股日成交資訊。
 *
 * 證交所的數值一律以字串回傳，沒有資料時為空字串，所以欄位都保留為 `String?`，
 * 交由 [com.jeremy.twsestock.data.mapper.StockMapper] 轉換成數字。
 */
@JsonClass(generateAdapter = true)
data class StockDayDto(
    /** 交易日期，民國年格式，例如 "1151008"。 */
    @Json(name = "Date") val date: String? = null,
    /** 證券代號。 */
    @Json(name = "Code") val code: String? = null,
    /** 證券名稱。 */
    @Json(name = "Name") val name: String? = null,
    /** 成交股數（股）。 */
    @Json(name = "TradeVolume") val tradeVolume: String? = null,
    /** 成交金額（元）。 */
    @Json(name = "TradeValue") val tradeValue: String? = null,
    /** 開盤價。 */
    @Json(name = "OpeningPrice") val openingPrice: String? = null,
    /** 最高價。 */
    @Json(name = "HighestPrice") val highestPrice: String? = null,
    /** 最低價。 */
    @Json(name = "LowestPrice") val lowestPrice: String? = null,
    /** 收盤價。 */
    @Json(name = "ClosingPrice") val closingPrice: String? = null,
    /** 漲跌價差，例如 "-0.0900"。 */
    @Json(name = "Change") val change: String? = null,
    /** 成交筆數。 */
    @Json(name = "Transaction") val transaction: String? = null,
)
