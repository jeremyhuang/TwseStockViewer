package com.jeremy.twsestock.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.jeremy.twsestock.R
import com.jeremy.twsestock.databinding.FragmentStockListBinding
import com.jeremy.twsestock.ui.common.toDateText
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/** 主畫面：上市個股列表。 */
class StockListFragment : Fragment(R.layout.fragment_stock_list) {

    private val viewModel: StockListViewModel by viewModel()

    private var _binding: FragmentStockListBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val stockAdapter = StockAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStockListBinding.bind(view)
        setupToolbar()
        setupList()
        observeUiState()
    }

    override fun onDestroyView() {
        // Adapter 的生命週期比 View 長，先解除綁定，避免 RecyclerView 洩漏。
        binding.rvStockList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun setupToolbar() {
        binding.tbStockList.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_sort -> {
                    // TODO: 開啟排序 Bottom Sheet。
                    true
                }
                else -> false
            }
        }
    }

    private fun setupList() {
        binding.rvStockList.apply {
            // 直向一欄、橫向兩欄（見 values-land/integers.xml）。
            layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.stock_list_span_count))
            adapter = stockAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: StockListUiState) {
        binding.tbStockList.subtitle = state.tradeDate?.let {
            getString(R.string.format_update_time, it.toDateText())
        }
        stockAdapter.submitList(state.stocks)
    }
}
