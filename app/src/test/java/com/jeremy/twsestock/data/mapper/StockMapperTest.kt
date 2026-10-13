package com.jeremy.twsestock.data.mapper

import com.jeremy.twsestock.data.model.PriceTrend
import com.jeremy.twsestock.data.remote.dto.StockDayAvgDto
import com.jeremy.twsestock.data.remote.dto.StockDayDto
import com.jeremy.twsestock.data.remote.dto.ValuationDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class StockMapperTest {

    private val mapper = StockMapper()
    private val fetchedAt = LocalDateTime.of(2026, 10, 13, 17, 20, 35)

    private val tsmcDaily = StockDayDto(
        date = "1151008", code = "2330", name = "台積電",
        tradeVolume = "32451203", tradeValue = "34236000000",
        openingPrice = "1045.00", highestPrice = "1060.00", lowestPrice = "1040.00",
        closingPrice = "1055.00", change = "15.0000", transaction = "45210",
    )

    @Test
    fun `三支 API 依代號合併成一筆 Stock`() {
        val snapshot = mapper.map(
            dailyTrading = listOf(tsmcDaily),
            monthlyAverages = listOf(StockDayAvgDto(code = "2330", monthlyAveragePrice = "1032.50")),
            valuations = listOf(ValuationDto(code = "2330", peRatio = "25.12", dividendYield = "1.52", pbRatio = "7.45")),
            fetchedAt = fetchedAt,
        )

        assertEquals(LocalDate.of(2026, 10, 8), snapshot.tradeDate)
        assertEquals(fetchedAt, snapshot.fetchedAt)
        val stock = snapshot.stocks.single()
        assertEquals("台積電", stock.name)
        assertEquals("1055.00".toBigDecimal(), stock.closingPrice)
        assertEquals("1032.50".toBigDecimal(), stock.monthlyAveragePrice)
        assertEquals(32_451_203L, stock.tradeVolume)
        assertEquals("25.12".toBigDecimal(), stock.peRatio)
        assertEquals(PriceTrend.UP, stock.closingVsMonthlyAverage)
        assertEquals(PriceTrend.UP, stock.changeTrend)
    }

    @Test
    fun `其他兩支 API 沒有該代號時，對應欄位為 null（left join）`() {
        val etf = StockDayDto(code = "0050", name = "元大台灣50", closingPrice = "180.00", change = "0.0000")

        val stock = mapper.map(listOf(etf), emptyList(), emptyList(), fetchedAt).stocks.single()

        assertNull(stock.monthlyAveragePrice)
        assertNull(stock.peRatio)
        assertEquals(PriceTrend.FLAT, stock.closingVsMonthlyAverage)
    }

    @Test
    fun `空字串欄位轉成 null，不影響其他欄位`() {
        val noTrade = StockDayDto(code = "00625K", name = "富邦上証+R", closingPrice = "", tradeVolume = "", change = "0.0000")
        val noPe = ValuationDto(code = "00625K", peRatio = "", dividendYield = "3.11")

        val stock = mapper.map(listOf(noTrade), emptyList(), listOf(noPe), fetchedAt).stocks.single()

        assertNull(stock.closingPrice)
        assertNull(stock.tradeVolume)
        assertNull(stock.peRatio)
        assertEquals("3.11".toBigDecimal(), stock.dividendYield)
    }

    @Test
    fun `代號為空的資料會被略過`() {
        val snapshot = mapper.map(listOf(StockDayDto(code = ""), tsmcDaily), emptyList(), emptyList(), fetchedAt)

        assertEquals(listOf("2330"), snapshot.stocks.map { it.code })
    }
}
