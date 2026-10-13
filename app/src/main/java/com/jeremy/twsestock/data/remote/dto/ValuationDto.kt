package com.jeremy.twsestock.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** `/exchangeReport/BWIBBU_ALL` 的單筆資料：上市個股日本益比、殖利率及股價淨值比。 */
@JsonClass(generateAdapter = true)
data class ValuationDto(
    /** 交易日期，民國年格式，例如 "1151008"。 */
    @Json(name = "Date") val date: String? = null,
    /** 證券代號。 */
    @Json(name = "Code") val code: String? = null,
    /** 證券名稱。 */
    @Json(name = "Name") val name: String? = null,
    /** 本益比；虧損公司為空字串。 */
    @Json(name = "PEratio") val peRatio: String? = null,
    /** 殖利率（%）。 */
    @Json(name = "DividendYield") val dividendYield: String? = null,
    /** 股價淨值比。 */
    @Json(name = "PBratio") val pbRatio: String? = null,
)
