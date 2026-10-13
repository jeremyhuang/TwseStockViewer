package com.jeremy.twsestock.data.remote

import com.jeremy.twsestock.data.remote.dto.StockDayDto
import com.jeremy.twsestock.data.remote.dto.ValuationDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Test

/** 用證交所實際的回應格式，確認 DTO 的欄位名稱對得上。 */
class TwseDtoParsingTest {

    private val moshi = Moshi.Builder().build()

    @Test
    fun `解析 STOCK_DAY_ALL 回應`() {
        val json = """
            [{"Date":"1151008","Code":"00401A","Name":"主動摩根台灣鑫收","TradeVolume":"5380337",
              "TradeValue":"78208949","OpeningPrice":"14.57","HighestPrice":"14.57","LowestPrice":"14.47",
              "ClosingPrice":"14.53","Change":"-0.0900","Transaction":"1615"}]
        """.trimIndent()
        val type = Types.newParameterizedType(List::class.java, StockDayDto::class.java)

        val dto = moshi.adapter<List<StockDayDto>>(type).fromJson(json)!!.single()

        assertEquals("00401A", dto.code)
        assertEquals("14.53", dto.closingPrice)
        assertEquals("-0.0900", dto.change)
        assertEquals("1615", dto.transaction)
    }

    @Test
    fun `解析 BWIBBU_ALL 回應，空字串保留為空字串`() {
        val json = """[{"Date":"1151008","Code":"1101","Name":"台泥","PEratio":"","DividendYield":"3.11","PBratio":"0.83"}]"""
        val type = Types.newParameterizedType(List::class.java, ValuationDto::class.java)

        val dto = moshi.adapter<List<ValuationDto>>(type).fromJson(json)!!.single()

        assertEquals("", dto.peRatio)
        assertEquals("3.11", dto.dividendYield)
        assertEquals("0.83", dto.pbRatio)
    }
}
