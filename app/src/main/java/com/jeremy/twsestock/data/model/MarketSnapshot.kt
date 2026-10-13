package com.jeremy.twsestock.data.model

import java.time.LocalDate

/** 證交所最新一個交易日的所有上市個股資料。 */
data class MarketSnapshot(
    val tradeDate: LocalDate?,      // 資料的交易日期
    val stocks: List<Stock>,        // 所有上市個股
)
