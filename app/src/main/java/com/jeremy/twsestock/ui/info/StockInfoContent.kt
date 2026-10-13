package com.jeremy.twsestock.ui.info

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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

/**
 * 個股估值資訊卡片：代號、名稱、資料時間，以及本益比、殖利率、股價淨值比三列數值。
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
    Surface(
        modifier = modifier.width(312.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp)) {
            StockInfoHeader(code = code, name = name, updatedAt = updatedAt)
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ValuationRow(R.string.label_pe_ratio, R.string.hint_pe_ratio, peRatio.toRatioText())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ValuationRow(R.string.label_dividend_yield, R.string.hint_dividend_yield, dividendYield.toRatioText())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ValuationRow(R.string.label_pb_ratio, R.string.hint_pb_ratio, pbRatio.toRatioText())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onConfirm, modifier = Modifier.align(Alignment.End)) {
                Text(text = stringResource(R.string.action_confirm), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 卡片標題：代號標籤、名稱，以及下方的資料時間。
 *
 * @param code 股票代號。
 * @param name 股票名稱。
 * @param updatedAt App 取得資料的時間。
 */
@Composable
private fun StockInfoHeader(code: String, name: String, updatedAt: LocalDateTime?) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = code,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = TABULAR_NUMBERS),
            )
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = stringResource(R.string.format_stock_info_subtitle, updatedAt.toDateTimeText()),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
    }
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

@Preview(name = "淺色")
@Preview(name = "深色", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
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

@Preview(name = "虧損公司（無本益比）")
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
