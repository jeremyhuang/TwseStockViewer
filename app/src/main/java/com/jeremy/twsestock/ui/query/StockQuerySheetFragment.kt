package com.jeremy.twsestock.ui.query

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jeremy.twsestock.R
import com.jeremy.twsestock.ui.list.StockListViewModel
import com.jeremy.twsestock.ui.theme.StockTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

/**
 * 排序與篩選的 Bottom Sheet，內容以 Compose 繪製（見 [StockQuerySheet]）。
 *
 * 由 StockListFragment 以 childFragmentManager 開啟，與列表共用同一個 [StockListViewModel]，
 * 點選選項會立即更新列表。
 */
class StockQuerySheetFragment : BottomSheetDialogFragment() {

    private val viewModel: StockListViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun getTheme(): Int = R.style.ThemeOverlay_TwseStockViewer_BottomSheetDialog

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
            behavior.apply {
                maxHeight = sheetMaxHeight()
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isShouldRemoveExpandedCorners = false
            }
        }
    }

    /**
     * 面板的最大高度：螢幕高度扣掉狀態列，再留一點間距。
     *
     * @return 最大高度（px）。
     */
    private fun sheetMaxHeight(): Int {
        val rootView = requireActivity().window.decorView
        val statusBarHeight = ViewCompat.getRootWindowInsets(rootView)
            ?.getInsets(WindowInsetsCompat.Type.statusBars())
            ?.top
            ?: 0
        val topMargin = (TOP_MARGIN_DP * resources.displayMetrics.density).roundToInt()
        return rootView.height - statusBarHeight - topMargin
    }

    companion object {
        const val TAG = "StockQuerySheet"

        /** 面板頂端與狀態列之間保留的間距（dp）。 */
        private const val TOP_MARGIN_DP = 16
    }
}
