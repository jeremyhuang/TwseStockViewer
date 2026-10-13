package com.jeremy.twsestock.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 沒有資料（值為 null）時顯示的文字。 */
const val PLACEHOLDER = "-"

/** 1 張 = 1,000 股。 */
private const val SHARES_PER_LOT = 1_000L

/** 1 億 = 100,000,000。 */
private val HUNDRED_MILLION = BigDecimal(100_000_000)

/** 日期顯示格式，例如 2026/10/08。 */
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

/** 日期時間顯示格式，例如 2026/10/13 17:20:35。 */
private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

/**
 * 將價格格式化為千分位、兩位小數的文字。
 *
 * 例如 `1055` → `"1,055.00"`。
 *
 * @receiver 價格（元），null 代表證交所未提供。
 * @return 格式化後的價格；receiver 為 null 時回傳 [PLACEHOLDER]。
 */
fun BigDecimal?.toPriceText(): String =
    this?.let { String.format(Locale.TAIWAN, "%,.2f", it) } ?: PLACEHOLDER

/**
 * 將漲跌價差與漲跌幅組成「符號 價差 (漲跌幅%)」的文字。
 *
 * 價差與漲跌幅都以絕對值顯示，方向由符號表示：▲ 漲、▼ 跌、— 平盤。
 * 例如 `15` 與 `1.44` → `"▲ 15.00 (1.44%)"`。
 *
 * @receiver 漲跌價差（元），null 代表證交所未提供。
 * @param percent 漲跌幅（%），null 時以 [PLACEHOLDER] 顯示。
 * @return 格式化後的漲跌文字；receiver 為 null 時回傳 [PLACEHOLDER]。
 */
fun BigDecimal?.toChangeText(percent: BigDecimal?): String {
    if (this == null) return PLACEHOLDER
    val symbol = when (signum()) {
        1 -> "▲"
        -1 -> "▼"
        else -> "—"
    }
    val percentText = percent?.let { String.format(Locale.TAIWAN, "%.2f%%", it.abs()) } ?: PLACEHOLDER
    return "$symbol ${abs().toPriceText()} ($percentText)"
}

/**
 * 將數量格式化為千分位整數。
 *
 * 例如 `45210` → `"45,210"`。
 *
 * @receiver 數量，例如成交筆數；null 代表證交所未提供。
 * @return 格式化後的數量；receiver 為 null 時回傳 [PLACEHOLDER]。
 */
fun Long?.toCountText(): String =
    this?.let { String.format(Locale.TAIWAN, "%,d", it) } ?: PLACEHOLDER

/**
 * 將成交股數換算成張數（無條件捨去），再格式化為千分位整數。
 *
 * 例如 `32451203` 股 → `"32,451"` 張。
 *
 * @receiver 成交股數（股），null 代表證交所未提供。
 * @return 格式化後的張數；receiver 為 null 時回傳 [PLACEHOLDER]。
 */
fun Long?.toLotText(): String = this?.div(SHARES_PER_LOT).toCountText()

/**
 * 將金額換算成億元，四捨五入到小數一位。
 *
 * 例如 `34236000000` → `"342.4"`。回傳值不含「億」字，單位由 strings.xml 的格式字串加上。
 *
 * @receiver 金額（元），null 代表證交所未提供。
 * @return 以億元為單位的金額；receiver 為 null 時回傳 [PLACEHOLDER]。
 */
fun Long?.toHundredMillionText(): String =
    this?.let {
        BigDecimal(it).divide(HUNDRED_MILLION, 1, RoundingMode.HALF_UP).toPlainString()
    } ?: PLACEHOLDER

/**
 * 將日期格式化為「年/月/日」。
 *
 * 例如 `2026-10-08` → `"2026/10/08"`。
 *
 * @receiver 日期，null 代表沒有資料。
 * @return 格式化後的日期；receiver 為 null 時回傳 [PLACEHOLDER]。
 */
fun LocalDate?.toDateText(): String = this?.format(DATE_FORMATTER) ?: PLACEHOLDER

/**
 * 將日期時間格式化為「年/月/日 時:分:秒」。
 *
 * 例如 `2026-10-13T17:20:35` → `"2026/10/13 17:20:35"`。
 *
 * @receiver 日期時間，null 代表沒有資料。
 * @return 格式化後的日期時間；receiver 為 null 時回傳 [PLACEHOLDER]。
 */
fun LocalDateTime?.toDateTimeText(): String = this?.format(DATE_TIME_FORMATTER) ?: PLACEHOLDER
