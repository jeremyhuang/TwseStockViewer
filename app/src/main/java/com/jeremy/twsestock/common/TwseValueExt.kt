package com.jeremy.twsestock.common

import java.math.BigDecimal
import java.time.DateTimeException
import java.time.LocalDate

/*
 * 證交所字串欄位的寬鬆解析。
 *
 * 無法解析的值一律回傳 null，避免單一筆格式異常的資料讓整份清單載入失敗。
 */

/** 民國年與西元年的差距。 */
private const val ROC_YEAR_OFFSET = 1911

/** 代表「沒有資料」的字串。 */
private val PLACEHOLDERS = setOf("", "-", "--", "---", "N/A")

/**
 * 將證交所的數值字串轉成 [BigDecimal]。
 *
 * 支援千分位與正號，例如 `"1,234.50"`、`"+0.50"`、`"-0.0900"`。
 *
 * @receiver API 回傳的字串，null 代表欄位不存在。
 * @return 解析後的數值；空字串、`"--"` 等佔位符或格式錯誤時為 null。
 */
internal fun String?.toTwseDecimal(): BigDecimal? {
    val cleaned = this?.trim()?.replace(",", "")?.removePrefix("+") ?: return null
    if (cleaned in PLACEHOLDERS) return null
    return cleaned.toBigDecimalOrNull()
}

/**
 * 將證交所的整數字串轉成 [Long]，例如成交股數。
 *
 * @receiver API 回傳的字串，null 代表欄位不存在。
 * @return 解析後的整數；無法解析或含有小數時為 null。
 */
internal fun String?.toTwseLong(): Long? = toTwseDecimal()?.let { value ->
    try {
        value.longValueExact()
    } catch (_: ArithmeticException) {
        null
    }
}

/**
 * 將民國年日期字串轉成 [LocalDate]。
 *
 * 最後四碼為月日，前面為民國年，例如 `"1151008"` → 2026-10-08。
 *
 * @receiver API 回傳的日期字串，null 代表欄位不存在。
 * @return 解析後的日期；格式不符或日期不存在時為 null。
 */
internal fun String?.toRocDate(): LocalDate? =
    this?.trim()
        ?.takeIf { it.length in 6..7 && it.all(Char::isDigit) }
        ?.let { digits ->
            try {
                LocalDate.of(
                    digits.dropLast(4).toInt() + ROC_YEAR_OFFSET,
                    digits.takeLast(4).take(2).toInt(),
                    digits.takeLast(2).toInt(),
                )
            } catch (_: DateTimeException) {
                null
            }
        }
