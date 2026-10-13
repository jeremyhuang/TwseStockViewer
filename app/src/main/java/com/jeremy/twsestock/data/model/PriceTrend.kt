package com.jeremy.twsestock.data.model

import java.math.BigDecimal

/** 價格趨勢，用來決定顯示紅色或綠色：漲為紅、跌為綠。 */
enum class PriceTrend {
    UP,
    DOWN,
    FLAT,
    ;

    companion object {
        /**
         * 比較兩個數值，得出趨勢。
         *
         * 使用 [BigDecimal.compareTo] 而非 `equals`，所以 `1.50` 與 `1.5000` 視為相等。
         *
         * @param value 要比較的數值，例如收盤價。
         * @param base 比較基準，例如月平均價。
         * @return [value] 大於 [base] 為 [UP]、小於為 [DOWN]；相等或任一為 null 時為 [FLAT]。
         */
        fun of(value: BigDecimal?, base: BigDecimal?): PriceTrend {
            if (value == null || base == null) return FLAT
            val result = value.compareTo(base)
            return when {
                result > 0 -> UP
                result < 0 -> DOWN
                else -> FLAT
            }
        }
    }
}
