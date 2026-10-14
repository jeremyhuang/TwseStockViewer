package com.jeremy.twsestock.ui.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeremy.twsestock.data.repository.StockRepository
import com.jeremy.twsestock.ui.query.AverageFilter
import com.jeremy.twsestock.ui.query.SecurityType
import com.jeremy.twsestock.ui.query.SortField
import com.jeremy.twsestock.ui.query.SortOrder
import com.jeremy.twsestock.ui.query.StockQuery
import com.jeremy.twsestock.ui.query.TrendFilter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val TAG = "StockListViewModel"

/**
 * 股票列表畫面的 ViewModel，同時提供排序與篩選面板使用。
 *
 * 保留完整的個股資料與查詢條件，任一邊改變時重新算出畫面要顯示的列表。
 *
 * @param repository 上市個股資料的來源。
 */
class StockListViewModel(
    private val repository: StockRepository,
) : ViewModel() {

    private val isLoading = MutableStateFlow(true)
    private val updatedAt = MutableStateFlow<LocalDateTime?>(null)
    private val allStocks = MutableStateFlow<List<StockUiModel>>(emptyList())
    private val query = MutableStateFlow(StockQuery())

    val uiState: StateFlow<StockListUiState> =
        combine(isLoading, updatedAt, allStocks, query) { isLoading, updatedAt, stocks, query ->
            StockListUiState(
                isLoading = isLoading,
                updatedAt = updatedAt,
                query = query,
                totalCount = stocks.size,
                stocks = query.filterAndSort(stocks),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = StockListUiState(),
        )

    init {
        loadStocks()
    }

    /**
     * 選擇排序欄位。
     *
     * @param field 排序欄位。
     */
    fun onSortFieldSelect(field: SortField) = query.update { it.copy(sortField = field) }

    /**
     * 選擇排序方向。
     *
     * @param order 排序方向。
     */
    fun onSortOrderSelect(order: SortOrder) = query.update { it.copy(sortOrder = order) }

    /**
     * 切換今日漲跌篩選；選已選的條件會取消。
     *
     * @param filter 被點選的條件。
     */
    fun onTrendToggle(filter: TrendFilter) = query.update { it.toggled(filter) }

    /**
     * 切換月均價篩選；選已選的條件會取消。
     *
     * @param filter 被點選的條件。
     */
    fun onAverageToggle(filter: AverageFilter) = query.update { it.toggled(filter) }

    /**
     * 切換證券類型篩選；選已選的條件會取消。
     *
     * @param filter 被點選的條件。
     */
    fun onTypeToggle(filter: SecurityType) = query.update { it.toggled(filter) }

    /** 排序與篩選全部回到預設值。 */
    fun onQueryReset() {
        query.value = StockQuery()
    }

    /** 清除所有篩選條件，保留排序。 */
    fun onFiltersClear() = query.update { it.withoutFilters() }

    private fun loadStocks() {
        viewModelScope.launch {
            try {
                val snapshot = repository.getMarketSnapshot()
                updatedAt.value = snapshot.fetchedAt
                allStocks.value = snapshot.stocks.map { stock -> stock.toUiModel() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "載入股票資料失敗", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    private companion object {
        /** 畫面離開（例如旋轉）後保留上游 Flow 的時間，避免重建時重新計算。 */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
