package com.jeremy.twsestock.ui.query

import com.jeremy.twsestock.data.model.PriceTrend
import com.jeremy.twsestock.ui.list.StockUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `預設依成交量降序排列`() {
        val stocks = listOf(stock("2330", tradeVolume = 10), stock("2317", tradeVolume = 30), stock("2603", tradeVolume = 20))

        val result = StockQuery().filterAndSort(stocks)

        assertEquals(listOf("2317", "2603", "2330"), result.map { it.code })
    }

    @Test
    fun `排序欄位為 null 的個股，升降序都排在最後`() {
        val stocks = listOf(stock("1101", peRatio = null), stock("2330", peRatio = "25"), stock("2882", peRatio = "10"))

        val desc = StockQuery(sortField = SortField.PE_RATIO, sortOrder = SortOrder.DESCENDING).filterAndSort(stocks)
        val asc = StockQuery(sortField = SortField.PE_RATIO, sortOrder = SortOrder.ASCENDING).filterAndSort(stocks)

        assertEquals(listOf("2330", "2882", "1101"), desc.map { it.code })
        assertEquals(listOf("2882", "2330", "1101"), asc.map { it.code })
    }

    @Test
    fun `數值相同時依代號排序`() {
        val stocks = listOf(stock("2603", tradeVolume = 5), stock("2330", tradeVolume = 5))

        assertEquals(listOf("2330", "2603"), StockQuery().filterAndSort(stocks).map { it.code })
    }

    @Test
    fun `平盤只包含有收盤價且漲跌為 0 的個股`() {
        val flat = stock("2412", change = "0")
        val noTrade = stock("9999", change = null, closingPrice = null)
        val up = stock("2330", change = "15")

        val result = StockQuery(trend = TrendFilter.FLAT).filterAndSort(listOf(flat, noTrade, up))

        assertEquals(listOf("2412"), result.map { it.code })
    }

    @Test
    fun `月均價篩選依收盤價與月均價的比較結果`() {
        val above = stock("2330", closingTrend = PriceTrend.UP)
        val below = stock("2882", closingTrend = PriceTrend.DOWN)
        val noAverage = stock("0050", closingTrend = PriceTrend.FLAT)

        val result = StockQuery(average = AverageFilter.BELOW).filterAndSort(listOf(above, below, noAverage))

        assertEquals(listOf("2882"), result.map { it.code })
    }

    @Test
    fun `類型依代號判斷：4 碼非 0 開頭為個股，00 開頭為 ETF，其他都不算`() {
        val stocks = listOf(stock("2330"), stock("0050"), stock("00878"), stock("2881A"), stock("020000"))

        assertEquals(listOf("2330"), StockQuery(type = SecurityType.STOCK).filterAndSort(stocks).map { it.code })
        assertEquals(setOf("0050", "00878"), StockQuery(type = SecurityType.ETF).filterAndSort(stocks).map { it.code }.toSet())
    }

    @Test
    fun `不同組的條件同時成立才會顯示`() {
        val stocks = listOf(
            stock("2330", change = "15"),
            stock("2317", change = "-2"),
            stock("0050", change = "1"),
        )

        val result = StockQuery(trend = TrendFilter.UP, type = SecurityType.STOCK).filterAndSort(stocks)

        assertEquals(listOf("2330"), result.map { it.code })
    }

    @Test
    fun `toggled 選已選的條件會取消，選其他的會替換`() {
        val up = StockQuery().toggled(TrendFilter.UP)

        assertEquals(TrendFilter.UP, up.trend)
        assertEquals(TrendFilter.DOWN, up.toggled(TrendFilter.DOWN).trend)
        assertNull(up.toggled(TrendFilter.UP).trend)
    }

    @Test
    fun `withoutFilters 清除篩選但保留排序`() {
        val query = StockQuery(sortField = SortField.CODE, trend = TrendFilter.UP, type = SecurityType.ETF)

        val cleared = query.withoutFilters()

        assertFalse(cleared.hasFilter)
        assertEquals(SortField.CODE, cleared.sortField)
        assertTrue(query.hasFilter)
    }
}
