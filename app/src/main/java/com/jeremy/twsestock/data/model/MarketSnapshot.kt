package com.jeremy.twsestock.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/** 證交所最新一個交易日的所有上市個股資料。 */
data class MarketSnapshot(
    val tradeDate: LocalDate?,      // 資料的交易日期
    val fetchedAt: LocalDateTime,   // App 取得資料的時間（API 只提供日期，沒有時間）
    val stocks: List<Stock>,        // 所有上市個股
)
