package com.jeremy.twsestock.data.remote

import com.jeremy.twsestock.data.remote.dto.StockDayAvgDto
import com.jeremy.twsestock.data.remote.dto.StockDayDto
import com.jeremy.twsestock.data.remote.dto.ValuationDto
import retrofit2.http.GET

/** 臺灣證券交易所 OpenAPI */
interface TwseApi {

    /**
     * 上市個股日成交資訊。
     *
     * @return 所有上市證券最新交易日的成交資訊
     */
    @GET("exchangeReport/STOCK_DAY_ALL")
    suspend fun getDailyTrading(): List<StockDayDto>

    /**
     * 上市個股日收盤價及月平均價。
     *
     * @return 所有上市證券的收盤價與月平均價
     */
    @GET("exchangeReport/STOCK_DAY_AVG_ALL")
    suspend fun getMonthlyAverages(): List<StockDayAvgDto>

    /**
     * 上市個股日本益比、殖利率及股價淨值比。
     *
     * @return 所有上市個股的估值資料
     */
    @GET("exchangeReport/BWIBBU_ALL")
    suspend fun getValuations(): List<ValuationDto>

    companion object {
        const val BASE_URL = "https://openapi.twse.com.tw/v1/"
    }
}
