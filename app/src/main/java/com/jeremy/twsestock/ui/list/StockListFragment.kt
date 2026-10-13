package com.jeremy.twsestock.ui.list

import androidx.fragment.app.Fragment
import com.jeremy.twsestock.R
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Main screen: the list of listed stocks. */
class StockListFragment : Fragment(R.layout.fragment_stock_list) {

    private val viewModel: StockListViewModel by viewModel()
}
