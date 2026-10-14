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
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
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

    /**
     * 載入狀態。
     *
     * @property isLoading 第一次載入或按「重試」時為 true，畫面中央顯示轉圈。
     * @property isRefreshing 下拉重新整理時為 true，顯示 SwipeRefreshLayout 的轉圈。
     * @property isFailed 載入失敗且目前沒有任何資料時為 true，畫面顯示錯誤提示。
     * @property updatedAt 最近一次成功取得資料的時間。
     */
    private data class LoadState(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val isFailed: Boolean = false,
        val updatedAt: LocalDateTime? = null,
    )

    private val loadState = MutableStateFlow(LoadState())
    private val allStocks = MutableStateFlow<List<StockUiModel>>(emptyList())
    private val query = MutableStateFlow(StockQuery())

    private val _events = Channel<StockListEvent>(Channel.BUFFERED)

    /** 只需要通知一次的事件，例如「更新失敗」的 Snackbar；旋轉後不會重複顯示。 */
    val events: Flow<StockListEvent> = _events.receiveAsFlow()

    /** 目前進行中的載入；載入中再觸發時直接忽略，避免重複請求。 */
    private var loadJob: Job? = null

    val uiState: StateFlow<StockListUiState> =
        combine(loadState, allStocks, query) { load, stocks, query ->
            StockListUiState(
                isLoading = load.isLoading,
                isRefreshing = load.isRefreshing,
                isLoadFailed = load.isFailed,
                updatedAt = load.updatedAt,
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

    /** 載入失敗後按「重試」：畫面中央顯示轉圈並重新載入。 */
    fun onRetry() = loadStocks(isRefresh = false)

    /** 下拉重新整理：保留目前的列表，重新取得最新資料。 */
    fun onRefresh() = loadStocks(isRefresh = true)

    /**
     * 向 Repository 取得資料。
     *
     * 失敗時：若目前沒有任何資料，顯示錯誤畫面；若已有資料（例如下拉重新整理失敗），保留舊資料並送出
     * [StockListEvent.RefreshFailed]，讓畫面以 Snackbar 提示。
     *
     * @param isRefresh 是否為下拉重新整理。
     */
    private fun loadStocks(isRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            loadState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, isFailed = false) }
            try {
                val snapshot = repository.getMarketSnapshot()
                allStocks.value = snapshot.stocks.map { stock -> stock.toUiModel() }
                loadState.update { it.copy(updatedAt = snapshot.fetchedAt) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "載入股票資料失敗", e)
                if (allStocks.value.isEmpty()) {
                    loadState.update { it.copy(isFailed = true) }
                } else {
                    _events.send(StockListEvent.RefreshFailed)
                }
            } finally {
                loadState.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    private companion object {
        /** 畫面離開（例如旋轉）後保留上游 Flow 的時間，避免重建時重新計算。 */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
