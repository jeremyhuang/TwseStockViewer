package com.jeremy.twsestock.ui.list

import com.jeremy.twsestock.data.mapper.StockMapper
import com.jeremy.twsestock.data.repository.StockRepository
import com.jeremy.twsestock.testutil.FakeTwseApi
import com.jeremy.twsestock.testutil.MainDispatcherRule
import com.jeremy.twsestock.ui.query.SecurityType
import com.jeremy.twsestock.ui.query.SortField
import com.jeremy.twsestock.ui.query.SortOrder
import com.jeremy.twsestock.ui.query.StockQuery
import com.jeremy.twsestock.ui.query.TrendFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StockListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedClock = Clock.fixed(Instant.parse("2026-10-13T09:20:35Z"), ZoneId.of("Asia/Taipei"))

    private fun createViewModel(api: FakeTwseApi = FakeTwseApi()) = StockListViewModel(
        repository = StockRepository(
            api = api,
            mapper = StockMapper(),
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            clock = fixedClock,
        ),
    )

    /**
     * uiState 用 WhileSubscribed 建立，沒有人收集時不會更新；在背景開一個收集者，讓 value 保持最新。
     *
     * @receiver 測試的 coroutine scope。
     * @param viewModel 要觀察的 ViewModel。
     */
    private fun TestScope.observe(viewModel: StockListViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private val StockListViewModel.state: StockListUiState
        get() = uiState.value

    private val StockListViewModel.codes: List<String>
        get() = state.stocks.map { it.code }

    /**
     * 載入成功後結束載入，並依預設的成交量降序顯示。
     *
     * 測試對象：StockListViewModel 建立時呼叫的 loadStocks（private，透過 [StockListViewModel.uiState] 觀察結果）
     */
    @Test
    fun loadStocks_success_stopsLoadingAndSortsByVolumeDescending() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)

        println("isLoading：期待 false，實際 ${viewModel.state.isLoading}")
        assertEquals(false, viewModel.state.isLoading)
        println("更新時間：期待 2026-10-13T17:20:35，實際 ${viewModel.state.updatedAt}")
        assertEquals(LocalDateTime.of(2026, 10, 13, 17, 20, 35), viewModel.state.updatedAt)
        println("總檔數：期待 3，實際 ${viewModel.state.totalCount}")
        assertEquals(3, viewModel.state.totalCount)
        println("列表順序（成交量降序）：期待 [2317, 2330, 0050]，實際 ${viewModel.codes}")
        assertEquals(listOf("2317", "2330", "0050"), viewModel.codes)
    }

    /**
     * 載入失敗時結束載入，列表為空，不顯示篩選無結果。
     *
     * 測試對象：StockListViewModel 建立時呼叫的 loadStocks（private，透過 [StockListViewModel.uiState] 觀察結果）
     */
    @Test
    fun loadStocks_failure_stopsLoadingWithEmptyListAndNoFilteredEmptyState() = runTest {
        val viewModel = createViewModel(FakeTwseApi(error = IOException("network")))
        observe(viewModel)

        println("isLoading：期待 false，實際 ${viewModel.state.isLoading}")
        assertEquals(false, viewModel.state.isLoading)
        println("列表筆數：期待 0，實際 ${viewModel.state.stocks.size}")
        assertEquals(0, viewModel.state.stocks.size)
        println("isFilteredEmpty：期待 false，實際 ${viewModel.state.isFilteredEmpty}")
        assertEquals(false, viewModel.state.isFilteredEmpty)
    }

    /**
     * 切換篩選條件會更新列表，再點一次同一個條件會取消。
     *
     * 測試對象：[StockListViewModel.onTrendToggle]
     */
    @Test
    fun onTrendToggle_toggleTwice_filtersThenClears() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onTrendToggle(TrendFilter.UP)
        println("點一次上漲的列表：期待 [2330, 0050]，實際 ${viewModel.codes}")
        assertEquals(listOf("2330", "0050"), viewModel.codes)

        viewModel.onTrendToggle(TrendFilter.UP)
        println("再點一次上漲的列表：期待 [2317, 2330, 0050]，實際 ${viewModel.codes}")
        assertEquals(listOf("2317", "2330", "0050"), viewModel.codes)
        println("再點一次後 hasFilter：期待 false，實際 ${viewModel.state.query.hasFilter}")
        assertEquals(false, viewModel.state.query.hasFilter)
    }

    /**
     * 變更排序欄位與方向會重新排序。
     *
     * 測試對象：[StockListViewModel.onSortFieldSelect]、[StockListViewModel.onSortOrderSelect]
     */
    @Test
    fun onSortSelect_fieldAndOrderChanged_resortsList() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onSortFieldSelect(SortField.CODE)
        viewModel.onSortOrderSelect(SortOrder.ASCENDING)

        println("代號升序：期待 [0050, 2317, 2330]，實際 ${viewModel.codes}")
        assertEquals(listOf("0050", "2317", "2330"), viewModel.codes)
    }

    /**
     * 篩選後沒有結果時標記為篩選無結果，清除篩選後恢復並保留排序。
     *
     * 測試對象：[StockListViewModel.onTypeToggle]、[StockListViewModel.onTrendToggle]、[StockListViewModel.onFiltersClear]、[StockListUiState.isFilteredEmpty]
     */
    @Test
    fun onFiltersClear_afterEmptyResult_restoresListAndKeepsSort() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)
        viewModel.onSortFieldSelect(SortField.CODE)

        viewModel.onTypeToggle(SecurityType.ETF)
        viewModel.onTrendToggle(TrendFilter.DOWN)
        println("ETF + 下跌的 isFilteredEmpty：期待 true，實際 ${viewModel.state.isFilteredEmpty}")
        assertEquals(true, viewModel.state.isFilteredEmpty)

        viewModel.onFiltersClear()
        println("清除後列表筆數：期待 3，實際 ${viewModel.codes.size}")
        assertEquals(3, viewModel.codes.size)
        println("清除後排序欄位：期待 CODE，實際 ${viewModel.state.query.sortField}")
        assertEquals(SortField.CODE, viewModel.state.query.sortField)
    }

    /**
     * 重設會讓排序與篩選回到預設值。
     *
     * 測試對象：[StockListViewModel.onQueryReset]
     */
    @Test
    fun onQueryReset_resetsSortAndFiltersToDefault() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)
        viewModel.onSortFieldSelect(SortField.PE_RATIO)
        viewModel.onTrendToggle(TrendFilter.UP)

        viewModel.onQueryReset()

        println("重設後的條件：期待 ${StockQuery()}，實際 ${viewModel.state.query}")
        assertEquals(StockQuery(), viewModel.state.query)
    }
}
