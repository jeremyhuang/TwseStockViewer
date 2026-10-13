package com.jeremy.twsestock.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeremy.twsestock.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 股票列表畫面的 ViewModel。
 *
 * @param repository 上市個股資料的來源。
 */
class StockListViewModel(
    private val repository: StockRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    init {
        loadStocks()
    }

    private fun loadStocks() {
        viewModelScope.launch {
            val snapshot = repository.getMarketSnapshot()
            _uiState.update {
                it.copy(
                    tradeDate = snapshot.tradeDate,
                    stocks = snapshot.stocks.map { stock -> stock.toUiModel() },
                )
            }
        }
    }
}
