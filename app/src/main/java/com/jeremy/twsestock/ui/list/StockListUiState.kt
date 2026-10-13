package com.jeremy.twsestock.ui.list

import java.time.LocalDate

/** 股票列表畫面的狀態。 */
data class StockListUiState(
    /** 資料的交易日期；尚未載入或 API 未提供時為 null，標題列不顯示副標題。 */
    val tradeDate: LocalDate? = null,
    /** 列表要顯示的個股。 */
    val stocks: List<StockUiModel> = emptyList(),
)
