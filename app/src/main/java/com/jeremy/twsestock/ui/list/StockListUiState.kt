package com.jeremy.twsestock.ui.list

import com.jeremy.twsestock.ui.query.StockQuery
import java.time.LocalDateTime

/** 股票列表畫面的狀態。 */
data class StockListUiState(
    /** 是否正在載入資料（第一次進入或按「重試」）；載入完成（成功或失敗）後為 false。 */
    val isLoading: Boolean = true,
    /** 是否正在下拉重新整理。 */
    val isRefreshing: Boolean = false,
    /** 載入失敗且沒有任何資料可顯示；畫面顯示錯誤提示與「重試」按鈕。 */
    val isLoadFailed: Boolean = false,
    /** App 取得資料的時間；尚未載入時為 null，標題列不顯示副標題。 */
    val updatedAt: LocalDateTime? = null,
    /** 目前的排序與篩選條件。 */
    val query: StockQuery = StockQuery(),
    /** 篩選前的總檔數。 */
    val totalCount: Int = 0,
    /** 依 [query] 篩選、排序後，列表要顯示的個股。 */
    val stocks: List<StockUiModel> = emptyList(),
) {
    /** 有資料，但套用篩選後一筆都不符合；畫面顯示「沒有符合條件的股票」。 */
    val isFilteredEmpty: Boolean
        get() = !isLoading && query.hasFilter && totalCount > 0 && stocks.isEmpty()
}
