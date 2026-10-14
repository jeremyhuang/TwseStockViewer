package com.jeremy.twsestock.common

import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.jeremy.twsestock.R
import com.jeremy.twsestock.data.model.PriceTrend

/**
 * 取得價格趨勢對應的文字顏色（台股慣例：紅漲、綠跌、平盤灰色）。
 *
 * 紅綠色來自 `R.color.stock_up` / `R.color.stock_down`；
 * 平盤使用主題的次要文字色 `colorOnSurfaceVariant`。
 *
 * @receiver 價格趨勢。
 * @param view 用來取得目前主題與 Context 的 View。
 * @return 可直接設給 `setTextColor` 的顏色值。
 */
@ColorInt
fun PriceTrend.resolveColor(view: View): Int = when (this) {
    PriceTrend.UP -> ContextCompat.getColor(view.context, R.color.stock_up)
    PriceTrend.DOWN -> ContextCompat.getColor(view.context, R.color.stock_down)
    PriceTrend.FLAT -> MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurfaceVariant)
}
