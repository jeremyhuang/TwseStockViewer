package com.jeremy.twsestock.ui.query

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jeremy.twsestock.ui.list.StockListViewModel
import com.jeremy.twsestock.ui.theme.StockTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 排序與篩選的 Bottom Sheet，內容以 Compose 繪製（見 [StockQuerySheet]）。
 *
 * 由 StockListFragment 以 childFragmentManager 開啟，與列表共用同一個 [StockListViewModel]，
 * 點選選項會立即更新列表。
 */
class StockQuerySheetFragment : BottomSheetDialogFragment() {

    private val viewModel: StockListViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                StockTheme {
                    StockQuerySheet(
                        query = state.query,
                        resultCount = state.stocks.size,
                        onSortFieldSelect = viewModel::onSortFieldSelect,
                        onSortOrderSelect = viewModel::onSortOrderSelect,
                        onTrendToggle = viewModel::onTrendToggle,
                        onAverageToggle = viewModel::onAverageToggle,
                        onTypeToggle = viewModel::onTypeToggle,
                        onReset = viewModel::onQueryReset,
                    )
                }
            }
        }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.apply {
            // 圓角與背景由 Compose 的 Surface 負責，拿掉 Material 預設的背景。
            findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.background = null
            behavior.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }
    }

    companion object {
        const val TAG = "StockQuerySheet"
    }
}
