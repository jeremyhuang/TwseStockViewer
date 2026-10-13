package com.jeremy.twsestock.ui.list

import java.time.LocalDate
import java.time.LocalDateTime

/** 股票列表畫面的狀態。 */
data class StockListUiState(
    /** 是否正在載入資料；第一次進入畫面時為 true，載入完成（成功或失敗）後為 false。 */
    val isLoading: Boolean = true,
    /** 資料的交易日期；API 未提供時為 null。 */
    val tradeDate: LocalDate? = null,
    /** App 取得資料的時間；尚未載入時為 null，標題列不顯示副標題。 */
    val updatedAt: LocalDateTime? = null,
    /** 列表要顯示的個股。 */
    val stocks: List<StockUiModel> = emptyList(),
)
