package com.jeremy.twsestock.ui.query

import com.jeremy.twsestock.data.model.PriceTrend
import com.jeremy.twsestock.ui.list.StockUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class StockQueryTest {

    private fun stock(
        code: String,
        change: String? = "1",
        closingPrice: String? = "100",
        closingTrend: PriceTrend = PriceTrend.FLAT,
        tradeVolume: Long? = 1_000,
        peRatio: String? = null,
    ): StockUiModel {
        val changeValue = change?.toBigDecimal()
        return StockUiModel(
            code = code,
            name = code,
            closingPrice = closingPrice?.toBigDecimal(),
            closingTrend = closingTrend,
            change = changeValue,
            changePercent = null,
            changeTrend = PriceTrend.of(changeValue, BigDecimal.ZERO),
            monthlyAveragePrice = null,
            openingPrice = null,
            highestPrice = null,
            lowestPrice = null,
            tradeVolume = tradeVolume,
            transactionCount = null,
            tradeValue = null,
            peRatio = peRatio?.toBigDecimal(),
            dividendYield = null,
            pbRatio = null,
        )
    }

    /** 套用條件後的代號，用來比對結果順序。 */
    private fun StockQuery.codesOf(stocks: List<StockUiModel>): List<String> = filterAndSort(stocks).map { it.code }

    /**
     * 預設依成交量降序排列。
     *
     * 測試對象：[StockQuery.filterAndSort]
     */
    @Test
    fun filterAndSort_defaultQuery_sortsByVolumeDescending() {
        val stocks = listOf(stock("2330", tradeVolume = 10), stock("2317", tradeVolume = 30), stock("2603", tradeVolume = 20))

        val actual = StockQuery().codesOf(stocks)
        println("輸入 2330(10) 2317(30) 2603(20)：期待 [2317, 2603, 2330]，實際 $actual")
        assertEquals(listOf("2317", "2603", "2330"), actual)
    }

    /**
     * 排序欄位為 null 的個股，升降序都排在最後。
     *
     * 測試對象：[StockQuery.filterAndSort]
     */
    @Test
    fun filterAndSort_nullSortValue_placedLastInBothOrders() {
        val stocks = listOf(stock("1101", peRatio = null), stock("2330", peRatio = "25"), stock("2882", peRatio = "10"))

        val desc = StockQuery(sortField = SortField.PE_RATIO, sortOrder = SortOrder.DESCENDING).codesOf(stocks)
        println("本益比降序：期待 [2330, 2882, 1101]，實際 $desc")
        assertEquals(listOf("2330", "2882", "1101"), desc)

        val asc = StockQuery(sortField = SortField.PE_RATIO, sortOrder = SortOrder.ASCENDING).codesOf(stocks)
        println("本益比升序：期待 [2882, 2330, 1101]，實際 $asc")
        assertEquals(listOf("2882", "2330", "1101"), asc)
    }

    /**
     * 數值相同時依代號排序。
     *
     * 測試對象：[StockQuery.filterAndSort]
     */
    @Test
    fun filterAndSort_equalValues_sortsByCode() {
        val stocks = listOf(stock("2603", tradeVolume = 5), stock("2330", tradeVolume = 5))

        val actual = StockQuery().codesOf(stocks)
        println("輸入 2603、2330（成交量都是 5）：期待 [2330, 2603]，實際 $actual")
        assertEquals(listOf("2330", "2603"), actual)
    }

    /**
     * 平盤只包含有收盤價且漲跌為 0 的個股。
     *
     * 測試對象：[StockQuery.filterAndSort]、[TrendFilter.matches]
     */
    @Test
    fun filterAndSort_flatTrend_excludesStocksWithoutTrades() {
        val stocks = listOf(
            stock("2412", change = "0"),
            stock("9999", change = null, closingPrice = null),
            stock("2330", change = "15"),
        )

        val actual = StockQuery(trend = TrendFilter.FLAT).codesOf(stocks)
        println("2412 漲跌 0、9999 無成交、2330 上漲，篩選平盤：期待 [2412]，實際 $actual")
        assertEquals(listOf("2412"), actual)
    }

    /**
     * 月均價篩選依收盤價與月均價的比較結果。
     *
     * 測試對象：[StockQuery.filterAndSort]、[AverageFilter.matches]
     */
    @Test
    fun filterAndSort_averageFilter_usesClosingVsMonthlyAverage() {
        val stocks = listOf(
            stock("2330", closingTrend = PriceTrend.UP),
            stock("2882", closingTrend = PriceTrend.DOWN),
            stock("0050", closingTrend = PriceTrend.FLAT),
        )

        val above = StockQuery(average = AverageFilter.ABOVE).codesOf(stocks)
        println("高於月均：期待 [2330]，實際 $above")
        assertEquals(listOf("2330"), above)

        val below = StockQuery(average = AverageFilter.BELOW).codesOf(stocks)
        println("低於月均：期待 [2882]，實際 $below")
        assertEquals(listOf("2882"), below)
    }

    /**
     * 類型依代號判斷：4 碼非 0 開頭為個股，00 開頭為 ETF，其他都不算。
     *
     * 測試對象：[SecurityType.matches]
     */
    @Test
    fun filterAndSort_securityType_classifiesByCodePattern() {
        val cases = listOf(
            "2330" to SecurityType.STOCK,
            "0050" to SecurityType.ETF,
            "00878" to SecurityType.ETF,
            "2881A" to null,
            "020000" to null,
        )
        cases.forEach { (code, expected) ->
            val actual = SecurityType.entries.singleOrNull { it.matches(stock(code)) }
            println("代號 $code：期待 $expected，實際 $actual")
            assertEquals("代號 $code", expected, actual)
        }
    }

    /**
     * 不同組的條件同時成立才會顯示。
     *
     * 測試對象：[StockQuery.filterAndSort]
     */
    @Test
    fun filterAndSort_multipleGroups_requiresAllConditions() {
        val stocks = listOf(
            stock("2330", change = "15"),
            stock("2317", change = "-2"),
            stock("0050", change = "1"),
        )

        val actual = StockQuery(trend = TrendFilter.UP, type = SecurityType.STOCK).codesOf(stocks)
        println("2330 上漲個股、2317 下跌個股、0050 上漲 ETF，篩選上漲 + 個股：期待 [2330]，實際 $actual")
        assertEquals(listOf("2330"), actual)
    }

    /**
     * toggled 選已選的條件會取消，選其他的會替換。
     *
     * 測試對象：[StockQuery.toggled]
     */
    @Test
    fun toggled_sameFilter_clearsOtherwiseReplaces() {
        val up = StockQuery().toggled(TrendFilter.UP)
        println("無 → 點上漲：期待 UP，實際 ${up.trend}")
        assertEquals(TrendFilter.UP, up.trend)

        val down = up.toggled(TrendFilter.DOWN)
        println("上漲 → 點下跌：期待 DOWN，實際 ${down.trend}")
        assertEquals(TrendFilter.DOWN, down.trend)

        val cleared = up.toggled(TrendFilter.UP)
        println("上漲 → 再點上漲：期待 null，實際 ${cleared.trend}")
        assertEquals(null, cleared.trend)
    }

    /**
     * withoutFilters 清除篩選但保留排序。
     *
     * 測試對象：[StockQuery.withoutFilters]、[StockQuery.hasFilter]
     */
    @Test
    fun withoutFilters_clearsFiltersAndKeepsSort() {
        val query = StockQuery(sortField = SortField.CODE, trend = TrendFilter.UP, type = SecurityType.ETF)
        val cleared = query.withoutFilters()

        println("清除後 hasFilter：期待 false，實際 ${cleared.hasFilter}")
        assertEquals(false, cleared.hasFilter)
        println("清除後排序欄位：期待 CODE，實際 ${cleared.sortField}")
        assertEquals(SortField.CODE, cleared.sortField)
        println("原本的 query hasFilter（不應被修改）：期待 true，實際 ${query.hasFilter}")
        assertEquals(true, query.hasFilter)
    }
}
