package com.jeremy.twsestock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.jeremy.twsestock.R

/**
 * Compose 畫面使用的主題。
 *
 * 顏色直接讀 colors.xml，跟 XML 畫面的 Material 主題共用同一套色票，深淺色跟著系統設定切換。
 *
 * @param darkTheme 是否使用深色主題，預設跟隨系統。
 * @param content 套用主題的內容。
 */
@Composable
fun StockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colorResource(R.color.md_dark_primary),
            onPrimary = colorResource(R.color.md_dark_on_primary),
            primaryContainer = colorResource(R.color.md_dark_primary_container),
            onPrimaryContainer = colorResource(R.color.md_dark_on_primary_container),
            background = colorResource(R.color.md_dark_background),
            surface = colorResource(R.color.md_dark_surface),
            onSurface = colorResource(R.color.md_dark_on_surface),
            onSurfaceVariant = colorResource(R.color.md_dark_on_surface_variant),
            outline = colorResource(R.color.md_dark_outline),
            outlineVariant = colorResource(R.color.md_dark_outline),
        )
    } else {
        lightColorScheme(
            primary = colorResource(R.color.md_light_primary),
            onPrimary = colorResource(R.color.md_light_on_primary),
            primaryContainer = colorResource(R.color.md_light_primary_container),
            onPrimaryContainer = colorResource(R.color.md_light_on_primary_container),
            background = colorResource(R.color.md_light_background),
            surface = colorResource(R.color.md_light_surface),
            onSurface = colorResource(R.color.md_light_on_surface),
            onSurfaceVariant = colorResource(R.color.md_light_on_surface_variant),
            outline = colorResource(R.color.md_light_outline),
            outlineVariant = colorResource(R.color.md_light_outline),
        )
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
