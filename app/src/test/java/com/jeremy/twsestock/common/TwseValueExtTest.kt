package com.jeremy.twsestock.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TwseValueExtTest {

    @Test
    fun `toTwseDecimal 解析一般數字、負數、千分位與正號`() {
        assertEquals("1055.00".toBigDecimal(), "1055.00".toTwseDecimal())
        assertEquals("-0.0900".toBigDecimal(), "-0.0900".toTwseDecimal())
        assertEquals("1234.5".toBigDecimal(), "1,234.5".toTwseDecimal())
        assertEquals("0.5".toBigDecimal(), "+0.5".toTwseDecimal())
        assertEquals("16.15".toBigDecimal(), " 16.15 ".toTwseDecimal())
    }

    @Test
    fun `toTwseDecimal 遇到空字串、佔位符或格式錯誤時回傳 null`() {
        assertNull((null as String?).toTwseDecimal())
        assertNull("".toTwseDecimal())
        assertNull("--".toTwseDecimal())
        assertNull("N/A".toTwseDecimal())
        assertNull("abc".toTwseDecimal())
    }

    @Test
    fun `toTwseLong 解析整數，含小數或空值時回傳 null`() {
        assertEquals(28_722_889L, "28722889".toTwseLong())
        assertEquals(1_000L, "1,000".toTwseLong())
        assertNull("".toTwseLong())
        assertNull("12.5".toTwseLong())
    }

    @Test
    fun `toRocDate 將民國年轉成西元日期`() {
        assertEquals(LocalDate.of(2026, 10, 8), "1151008".toRocDate())
        assertEquals(LocalDate.of(2010, 1, 5), "990105".toRocDate())
    }

    @Test
    fun `toRocDate 遇到格式錯誤或不存在的日期時回傳 null`() {
        assertNull((null as String?).toRocDate())
        assertNull("".toRocDate())
        assertNull("2026-10-08".toRocDate())
        assertNull("1151332".toRocDate())
    }
}
