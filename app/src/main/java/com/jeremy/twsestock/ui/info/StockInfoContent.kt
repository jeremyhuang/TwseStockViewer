package com.jeremy.twsestock.ui.info

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeremy.twsestock.R
import com.jeremy.twsestock.common.toDateTimeText
import com.jeremy.twsestock.common.toRatioText
import com.jeremy.twsestock.ui.theme.StockTheme
import java.math.BigDecimal
import java.time.LocalDateTime

/** 數字使用等寬數字，讓上下列的位數對齊。 */
private const val TABULAR_NUMBERS = "tnum"

/** 螢幕寬度超過這個值（例如橫向）時，卡片改成左右兩欄。 */
private const val TWO_PANE_MIN_SCREEN_WIDTH_DP = 600

/** 直向（單欄）卡片寬度。 */
private val SINGLE_PANE_WIDTH = 312.dp

/** 橫向（左右兩欄）卡片寬度。 */
private val TWO_PANE_WIDTH = 560.dp

/**
 * 依螢幕寬度判斷卡片是否要用左右兩欄。
 *
 * @param screenWidthDp 螢幕寬度（dp）。
 * @return 要用兩欄時為 true。
 */
internal fun isStockInfoTwoPane(screenWidthDp: Int): Boolean = screenWidthDp >= TWO_PANE_MIN_SCREEN_WIDTH_DP

/**
 * 卡片寬度。Dialog 視窗也要設成同樣寬度，見 StockInfoDialogFragment.onStart。
 *
 * @param screenWidthDp 螢幕寬度（dp）。
 * @return 卡片寬度。
 */
internal fun stockInfoCardWidth(screenWidthDp: Int): Dp =
    if (isStockInfoTwoPane(screenWidthDp)) TWO_PANE_WIDTH else SINGLE_PANE_WIDTH

/**
 * 個股估值資訊卡片：代號、名稱、資料時間，以及本益比、殖利率、股價淨值比三列數值。
 *
 * 直向時由上往下排列；螢幕夠寬時（例如橫向）改成左邊放代號、名稱與資料時間，右邊放數值與按鈕，
 * 避免卡片太高超出螢幕。
 *
 * @param code 股票代號。
 * @param name 股票名稱。
 * @param updatedAt App 取得資料的時間，null 時顯示「-」。
 * @param peRatio 本益比，null 時顯示「-」。
 * @param dividendYield 殖利率（%），null 時顯示「-」。
 * @param pbRatio 股價淨值比，null 時顯示「-」。
 * @param onConfirm 點選「確定」時呼叫。
 * @param modifier 外部傳入的 Modifier。
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun StockInfoCard(
    code: String,
    name: String,
    updatedAt: LocalDateTime?,
    peRatio: BigDecimal?,
    dividendYield: BigDecimal?,
    pbRatio: BigDecimal?,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTwoPane = isStockInfoTwoPane(screenWidthDp)
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.width(stockInfoCardWidth(screenWidthDp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            val contentPadding = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp)
            if (isTwoPane) {
                Row(modifier = contentPadding, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StockInfoHeader(
                        modifier = Modifier.weight(0.4f),
                        code = code,
                        name = name,
                        updatedAt = updatedAt,
                        isStacked = true,
                    )
                    Column(modifier = Modifier.weight(0.6f)) {
                        ValuationList(
                            peRatio = peRatio,
                            dividendYield = dividendYield,
                            pbRatio = pbRatio,
                            showOuterDividers = false,
                        )
                        ConfirmButton(onClick = onConfirm, modifier = Modifier.align(Alignment.End))
                    }
                }
            } else {
                Column(modifier = contentPadding) {
                    StockInfoHeader(code = code, name = name, updatedAt = updatedAt)
                    Spacer(modifier = Modifier.height(16.dp))
                    ValuationList(peRatio = peRatio, dividendYield = dividendYield, pbRatio = pbRatio)
                    ConfirmButton(onClick = onConfirm, modifier = Modifier.align(Alignment.End))
                }
            }
        }
    }
}

/**
 * 三列估值數值，列與列之間有分隔線。
 *
 * @param peRatio 本益比。
 * @param dividendYield 殖利率（%）。
 * @param pbRatio 股價淨值比。
 * @param showOuterDividers 是否在最上方與最下方也畫分隔線；兩欄版面不需要。
 */
@Composable
private fun ValuationList(
    peRatio: BigDecimal?,
    dividendYield: BigDecimal?,
    pbRatio: BigDecimal?,
    showOuterDividers: Boolean = true,
) {
    Column {
        if (showOuterDividers) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ValuationRow(R.string.label_pe_ratio, R.string.hint_pe_ratio, peRatio.toRatioText())
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ValuationRow(R.string.label_dividend_yield, R.string.hint_dividend_yield, dividendYield.toRatioText())
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ValuationRow(R.string.label_pb_ratio, R.string.hint_pb_ratio, pbRatio.toRatioText())
        if (showOuterDividers) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/**
 * 右下角的「確定」按鈕。
 *
 * @param onClick 點選時呼叫。
 * @param modifier 外部傳入的 Modifier。
 */
@Composable
private fun ConfirmButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.padding(top = 4.dp)) {
        Text(text = stringResource(R.string.action_confirm), fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * 卡片標題：代號標籤、名稱，以及下方的資料時間。
 *
 * @param code 股票代號。
 * @param name 股票名稱。
 * @param updatedAt App 取得資料的時間。
 * @param isStacked 為 true 時名稱放在代號下方、不限行數（兩欄版面左欄較窄，名稱並排會被截斷）；
 * false 時代號與名稱並排、名稱一行。
 * @param modifier 外部傳入的 Modifier。
 */
@Composable
private fun StockInfoHeader(
    modifier: Modifier = Modifier,
    code: String,
    name: String,
    updatedAt: LocalDateTime?,
    isStacked: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (isStacked) {
            CodeTag(code = code)
            StockName(name = name, maxLines = Int.MAX_VALUE)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CodeTag(code = code)
                StockName(name = name, maxLines = 1)
            }
        }
        Text(
            text = stringResource(R.string.format_stock_info_subtitle, updatedAt.toDateTimeText()),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
    }
}

/**
 * 股票代號標籤：淺藍底、藍字的小方塊。
 *
 * @param code 股票代號。
 */
@Composable
private fun CodeTag(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = TABULAR_NUMBERS),
    )
}

/**
 * 股票名稱，超過行數時以「…」結尾。
 *
 * @param name 股票名稱。
 * @param maxLines 最多顯示幾行。
 */
@Composable
private fun StockName(name: String, maxLines: Int) {
    Text(
        text = name,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * 一列估值數值：左邊是名稱與計算方式，右邊是數值。
 *
 * @param labelRes 數值名稱，例如「本益比」。
 * @param hintRes 計算方式說明，例如「股價 ÷ 每股盈餘」。
 * @param value 已格式化的數值。
 */
@Composable
private fun ValuationRow(@StringRes labelRes: Int, @StringRes hintRes: Int, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(labelRes),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(hintRes),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = TABULAR_NUMBERS,
            ),
        )
    }
}

@Preview(name = "淺色", widthDp = 360)
@Preview(name = "深色", widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StockInfoCardPreview() {
    StockTheme {
        StockInfoCard(
            code = "2330",
            name = "台積電",
            updatedAt = LocalDateTime.of(2026, 10, 13, 17, 20, 35),
            peRatio = "25.12".toBigDecimal(),
            dividendYield = "1.52".toBigDecimal(),
            pbRatio = "7.45".toBigDecimal(),
            onConfirm = {},
        )
    }
}

@Preview(name = "虧損公司（無本益比）", widthDp = 360)
@Composable
private fun StockInfoCardNoPePreview() {
    StockTheme {
        StockInfoCard(
            code = "1101",
            name = "台泥",
            updatedAt = LocalDateTime.of(2026, 10, 13, 17, 20, 35),
            peRatio = null,
            dividendYield = "3.11".toBigDecimal(),
            pbRatio = "0.83".toBigDecimal(),
            onConfirm = {},
        )
    }
}

@Preview(name = "橫向（左右兩欄，長名稱）", widthDp = 640, heightDp = 360)
@Composable
private fun StockInfoCardTwoPanePreview() {
    StockTheme {
        StockInfoCard(
            code = "00940",
            name = "元大台灣價值高息",
            updatedAt = LocalDateTime.of(2026, 10, 13, 17, 20, 35),
            peRatio = null,
            dividendYield = null,
            pbRatio = null,
            onConfirm = {},
        )
    }
}
