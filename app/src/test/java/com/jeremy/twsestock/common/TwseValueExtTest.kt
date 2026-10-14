package com.jeremy.twsestock.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TwseValueExtTest {

    /**
     * toTwseDecimal 解析一般數字、負數、千分位與正號。
     *
     * 測試對象：[toTwseDecimal]（common/TwseValueExt.kt）
     */
    @Test
    fun toTwseDecimal_validNumbers_parsesSignsAndThousandSeparators() {
        listOf(
            "1055.00" to "1055.00".toBigDecimal(),
            "-0.0900" to "-0.0900".toBigDecimal(),
            "1,234.5" to "1234.5".toBigDecimal(),
            "+0.5" to "0.5".toBigDecimal(),
            " 16.15 " to "16.15".toBigDecimal(),
        ).forEach { (input, expected) ->
            val actual = input.toTwseDecimal()
            println("輸入 \"$input\"：期待 $expected，實際 $actual")
            assertEquals("輸入 \"$input\"", expected, actual)
        }
    }

    /**
     * toTwseDecimal 遇到空字串、佔位符或格式錯誤時回傳 null。
     *
     * 測試對象：[toTwseDecimal]（common/TwseValueExt.kt）
     */
    @Test
    fun toTwseDecimal_emptyPlaceholderOrInvalid_returnsNull() {
        listOf(null, "", "--", "N/A", "abc").forEach { input ->
            val actual = input.toTwseDecimal()
            println("輸入 \"$input\"：期待 null，實際 $actual")
            assertEquals("輸入 \"$input\"", null, actual)
        }
    }

    /**
     * toTwseLong 解析整數，含小數或空值時回傳 null。
     *
     * 測試對象：[toTwseLong]（common/TwseValueExt.kt）
     */
    @Test
    fun toTwseLong_integerOrInvalid_parsesOrReturnsNull() {
        listOf(
            "28722889" to 28_722_889L,
            "1,000" to 1_000L,
            "" to null,
            "12.5" to null,
        ).forEach { (input, expected) ->
            val actual = input.toTwseLong()
            println("輸入 \"$input\"：期待 $expected，實際 $actual")
            assertEquals("輸入 \"$input\"", expected, actual)
        }
    }

    /**
     * toRocDate 將民國年轉成西元日期。
     *
     * 測試對象：[toRocDate]（common/TwseValueExt.kt）
     */
    @Test
    fun toRocDate_rocDate_convertsToGregorian() {
        listOf(
            "1151008" to LocalDate.of(2026, 10, 8),
            "990105" to LocalDate.of(2010, 1, 5),
        ).forEach { (input, expected) ->
            val actual = input.toRocDate()
            println("輸入 \"$input\"：期待 $expected，實際 $actual")
            assertEquals("輸入 \"$input\"", expected, actual)
        }
    }

    /**
     * toRocDate 遇到格式錯誤或不存在的日期時回傳 null。
     *
     * 測試對象：[toRocDate]（common/TwseValueExt.kt）
     */
    @Test
    fun toRocDate_invalidOrNonexistentDate_returnsNull() {
        listOf(null, "", "2026-10-08", "1151332").forEach { input ->
            val actual = input.toRocDate()
            println("輸入 \"$input\"：期待 null，實際 $actual")
            assertEquals("輸入 \"$input\"", null, actual)
        }
    }
}
