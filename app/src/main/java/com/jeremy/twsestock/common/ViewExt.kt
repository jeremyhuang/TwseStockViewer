package com.jeremy.twsestock.common

import android.animation.ValueAnimator
import android.view.View
import androidx.core.view.isVisible

/** 淡入淡出的時間。 */
private const val FADE_DURATION_MS = 200L

/**
 * 以淡入淡出的方式顯示或隱藏 View。
 *
 * 系統關閉動畫時（開發者選項、無障礙設定或 UI 測試）直接切換，不等動畫結束，
 * 避免 View 停在半透明或延遲隱藏。
 *
 * @receiver 要顯示或隱藏的 View。
 * @param visible true 為淡入顯示，false 為淡出後設為 GONE。
 */
fun View.fadeVisible(visible: Boolean) {
    animate().cancel()
    if (!ValueAnimator.areAnimatorsEnabled()) {
        alpha = 1f
        isVisible = visible
        return
    }
    if (visible) {
        if (!isVisible) {
            alpha = 0f
            isVisible = true
        }
        animate().alpha(1f).setDuration(FADE_DURATION_MS)
    } else if (isVisible) {
        animate().alpha(0f).setDuration(FADE_DURATION_MS).withEndAction {
            isVisible = false
            alpha = 1f
        }
    }
}
