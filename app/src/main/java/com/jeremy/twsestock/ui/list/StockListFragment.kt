package com.jeremy.twsestock.ui.list

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.chip.Chip
import com.jeremy.twsestock.R
import com.jeremy.twsestock.common.toDateTimeText
import com.jeremy.twsestock.databinding.FragmentStockListBinding
import com.jeremy.twsestock.ui.info.StockInfoDialogFragment
import com.jeremy.twsestock.ui.query.AverageFilter
import com.jeremy.twsestock.ui.query.LabeledOption
import com.jeremy.twsestock.ui.query.SecurityType
import com.jeremy.twsestock.ui.query.StockQuery
import com.jeremy.twsestock.ui.query.StockQuerySheetFragment
import com.jeremy.twsestock.ui.query.TrendFilter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/** 主畫面：上市個股列表。 */
class StockListFragment : Fragment(R.layout.fragment_stock_list) {

    private val viewModel: StockListViewModel by viewModel()

    private var _binding: FragmentStockListBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val stockAdapter = StockAdapter(onItemClick = ::showStockInfo)

    /** 排序與篩選按鈕上的小紅點，有篩選條件時顯示。 */
    private var queryBadge: BadgeDrawable? = null

    /** 上一次畫到畫面上的查詢條件，用來判斷條件是否改變。 */
    private var renderedQuery: StockQuery? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStockListBinding.bind(view)
        renderedQuery = null
        setupToolbar()
        setupList()
        setupEmptyState()
        observeUiState()
    }

    override fun onDestroyView() {
        // Adapter 的生命週期比 View 長，先解除綁定，避免 RecyclerView 洩漏。
        binding.rvStockList.adapter = null
        queryBadge = null
        _binding = null
        super.onDestroyView()
    }

    @OptIn(ExperimentalBadgeUtils::class)
    private fun setupToolbar() {
        binding.tbStockList.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_query -> {
                    showQuerySheet()
                    true
                }
                else -> false
            }
        }
        queryBadge = BadgeDrawable.create(requireContext()).apply { isVisible = false }.also {
            BadgeUtils.attachBadgeDrawable(it, binding.tbStockList, R.id.action_query)
        }
    }

    private fun setupList() {
        binding.rvStockList.apply {
            layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.stock_list_span_count))
            adapter = stockAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupEmptyState() {
        binding.btnClearFilter.setOnClickListener { viewModel.onFiltersClear() }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: StockListUiState) {
        binding.pbLoading.isVisible = state.isLoading
        binding.llEmpty.isVisible = state.isFilteredEmpty
        binding.tbStockList.subtitle = state.updatedAt?.let { updatedAt ->
            if (state.query.hasFilter) {
                getString(R.string.format_update_time_filtered, updatedAt.toDateTimeText(), state.stocks.size)
            } else {
                getString(R.string.format_update_time, updatedAt.toDateTimeText())
            }
        }
        queryBadge?.isVisible = state.query.hasFilter

        val queryChanged = state.query != renderedQuery
        if (queryChanged) {
            renderQueryChips(state.query)
            renderedQuery = state.query
        }
        binding.hsvQuery.isVisible = !state.isLoading
        stockAdapter.submitList(state.stocks) {
            // 換了排序或篩選條件，回到列表最上面。
            // callback 是非同步的，執行時畫面可能已經銷毀，所以用 _binding。
            if (queryChanged) _binding?.rvStockList?.scrollToPosition(0)
        }
    }

    /**
     * 畫出標題列下方的條件 chip：第一個是目前的排序，後面是各個篩選條件（可點 × 移除）。
     *
     * @param query 目前的查詢條件。
     */
    private fun renderQueryChips(query: StockQuery) {
        val chipGroup = binding.cgQuery
        chipGroup.removeAllViews()

        val sortText = getString(
            R.string.format_sort_chip,
            getString(query.sortField.labelRes),
            getString(query.sortOrder.shortLabelRes),
        )
        // 樣式寫在 layout 與 styles.xml（Widget.TwseStockViewer.Chip.*），這裡只設定內容與點擊。
        val inflater = layoutInflater
        chipGroup.addView(
            (inflater.inflate(R.layout.chip_query_sort, chipGroup, false) as Chip).apply {
                text = sortText
                setChipIconResource(query.sortOrder.iconRes)
                setOnClickListener { showQuerySheet() }
            }
        )

        query.activeFilters.forEach { filter ->
            val label = getString(filter.labelRes)
            chipGroup.addView(
                (inflater.inflate(R.layout.chip_query_filter, chipGroup, false) as Chip).apply {
                    text = label
                    closeIconContentDescription = getString(R.string.format_remove_filter, label)
                    setOnClickListener { removeFilter(filter) }
                    setOnCloseIconClickListener { removeFilter(filter) }
                }
            )
        }
    }

    /**
     * 移除單一篩選條件：再切換一次同一個條件就會取消。
     *
     * @param filter 要移除的條件。
     */
    private fun removeFilter(filter: LabeledOption) {
        when (filter) {
            is TrendFilter -> viewModel.onTrendToggle(filter)
            is AverageFilter -> viewModel.onAverageToggle(filter)
            is SecurityType -> viewModel.onTypeToggle(filter)
        }
    }

    /** 開啟排序與篩選面板；已經開著時不重複開啟。 */
    private fun showQuerySheet() {
        if (childFragmentManager.findFragmentByTag(StockQuerySheetFragment.TAG) != null) return
        StockQuerySheetFragment().show(childFragmentManager, StockQuerySheetFragment.TAG)
    }

    /**
     * 顯示個股的估值資訊彈窗。
     *
     * 快速連點時只會開一個彈窗。
     *
     * @param stock 被點選的個股。
     */
    private fun showStockInfo(stock: StockUiModel) {
        if (childFragmentManager.findFragmentByTag(StockInfoDialogFragment.TAG) != null) return
        StockInfoDialogFragment.newInstance(stock, viewModel.uiState.value.updatedAt)
            .show(childFragmentManager, StockInfoDialogFragment.TAG)
    }
}
