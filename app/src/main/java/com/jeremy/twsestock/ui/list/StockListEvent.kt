package com.jeremy.twsestock.ui.list

/** 股票列表畫面只需要處理一次的事件（不屬於畫面狀態，旋轉後不應重複出現）。 */
sealed interface StockListEvent {

    /** 下拉重新整理失敗；畫面仍顯示上一次的資料，以 Snackbar 提示。 */
    data object RefreshFailed : StockListEvent
}
