package com.jeremy.twsestock.ui.query

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeremy.twsestock.R
import com.jeremy.twsestock.ui.theme.StockTheme

/** 面板寬度超過這個值（例如橫向）時，排序與篩選改成左右兩欄。 */
private val TWO_PANE_MIN_WIDTH = 560.dp

/**
 * 排序與篩選面板。點選任何選項都會立即套用，沒有「套用」按鈕。
 *
 * 直向時排序在上、篩選在下；面板夠寬時（例如橫向）改成左邊排序、右邊篩選，減少需要捲動的高度。
 *
 * @param query 目前的排序與篩選條件。
 * @param resultCount 目前條件下符合的檔數。
 * @param onSortFieldSelect 選擇排序欄位時呼叫。
 * @param onSortOrderSelect 選擇排序方向時呼叫。
 * @param onTrendToggle 點選今日漲跌條件時呼叫。
 * @param onAverageToggle 點選月均價條件時呼叫。
 * @param onTypeToggle 點選證券類型條件時呼叫。
 * @param onReset 點選「重設」時呼叫。
 * @param modifier 外部傳入的 Modifier。
 */
@Composable
fun StockQuerySheet(
    query: StockQuery,
    resultCount: Int,
    onSortFieldSelect: (SortField) -> Unit,
    onSortOrderSelect: (SortOrder) -> Unit,
    onTrendToggle: (TrendFilter) -> Unit,
    onAverageToggle: (AverageFilter) -> Unit,
    onTypeToggle: (SecurityType) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        BoxWithConstraints {
            val isTwoPane = maxWidth >= TWO_PANE_MIN_WIDTH
            Column(
                modifier = Modifier
                    .nestedScroll(rememberNestedScrollInteropConnection())
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
                SheetHeader(onReset = onReset)

                if (isTwoPane) {
                    val dividerColor = MaterialTheme.colorScheme.outlineVariant
                    Row(
                        modifier = Modifier.drawBehind {
                            val x = size.width / 2
                            drawLine(
                                color = dividerColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        },
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        SortSection(
                            query = query,
                            onSortFieldSelect = onSortFieldSelect,
                            onSortOrderSelect = onSortOrderSelect,
                            modifier = Modifier.weight(1f),
                        )
                        FilterSection(
                            query = query,
                            onTrendToggle = onTrendToggle,
                            onAverageToggle = onAverageToggle,
                            onTypeToggle = onTypeToggle,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    SortSection(
                        query = query,
                        onSortFieldSelect = onSortFieldSelect,
                        onSortOrderSelect = onSortOrderSelect,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    FilterSection(
                        query = query,
                        onTrendToggle = onTrendToggle,
                        onAverageToggle = onAverageToggle,
                        onTypeToggle = onTypeToggle,
                    )
                }

                Text(
                    text = stringResource(R.string.format_query_result_count, resultCount),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

/**
 * 排序區塊：排序欄位 chip 與降序／升序切換。
 *
 * @param query 目前的排序與篩選條件。
 * @param onSortFieldSelect 選擇排序欄位時呼叫。
 * @param onSortOrderSelect 選擇排序方向時呼叫。
 * @param modifier 外部傳入的 Modifier。
 */
@Composable
private fun SortSection(
    query: StockQuery,
    onSortFieldSelect: (SortField) -> Unit,
    onSortOrderSelect: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(R.string.query_section_sort)
        OptionChips(options = SortField.entries, selected = query.sortField, onClick = onSortFieldSelect)
        SortOrderSelector(selected = query.sortOrder, onSelect = onSortOrderSelect)
    }
}

/**
 * 篩選區塊：今日漲跌、收盤價 vs 月均價、類型三組條件。
 *
 * @param query 目前的排序與篩選條件。
 * @param onTrendToggle 點選今日漲跌條件時呼叫。
 * @param onAverageToggle 點選月均價條件時呼叫。
 * @param onTypeToggle 點選證券類型條件時呼叫。
 * @param modifier 外部傳入的 Modifier。
 */
@Composable
private fun FilterSection(
    query: StockQuery,
    onTrendToggle: (TrendFilter) -> Unit,
    onAverageToggle: (AverageFilter) -> Unit,
    onTypeToggle: (SecurityType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterGroup(R.string.query_section_trend, TrendFilter.entries, query.trend, onTrendToggle)
        FilterGroup(R.string.query_section_average, AverageFilter.entries, query.average, onAverageToggle)
        FilterGroup(R.string.query_section_type, SecurityType.entries, query.type, onTypeToggle)
    }
}

/**
 * 面板頂端的拖曳把手。
 *
 * @param modifier 外部傳入的 Modifier。
 */
@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 36.dp, height = 4.dp)
            .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
    )
}

/**
 * 標題列：「排序與篩選」與「重設」按鈕。
 *
 * @param onReset 點選「重設」時呼叫。
 */
@Composable
private fun SheetHeader(onReset: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.query_sheet_title),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        TextButton(onClick = onReset) {
            Text(text = stringResource(R.string.action_reset), fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * 區塊標題，例如「排序依據」。
 *
 * @param textRes 標題文字。
 */
@Composable
private fun SectionTitle(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )
}

/**
 * 一組篩選條件：標題加上一排 chip，單選、可取消。
 *
 * @param titleRes 組別標題。
 * @param options 這一組的所有選項。
 * @param selected 目前選中的選項，null 代表不篩選。
 * @param onToggle 點選選項時呼叫。
 */
@Composable
private fun <T : LabeledOption> FilterGroup(
    @StringRes titleRes: Int,
    options: List<T>,
    selected: T?,
    onToggle: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        SectionTitle(titleRes)
        OptionChips(options = options, selected = selected, onClick = onToggle)
    }
}

/**
 * 一排可換行的選項 chip，選中的顯示勾勾。
 *
 * @param options 所有選項。
 * @param selected 目前選中的選項。
 * @param onClick 點選選項時呼叫。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T : LabeledOption> OptionChips(
    options: List<T>,
    selected: T?,
    onClick: (T) -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onClick(option) },
                    label = { Text(text = stringResource(option.labelRes), fontSize = 14.sp) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        selectedBorderColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

/**
 * 排序方向切換：降序／升序。
 *
 * @param selected 目前的排序方向。
 * @param onSelect 選擇排序方向時呼叫。
 */
@Composable
private fun SortOrderSelector(selected: SortOrder, onSelect: (SortOrder) -> Unit) {
    val orders = SortOrder.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        orders.forEachIndexed { index, order ->
            SegmentedButton(
                selected = order == selected,
                onClick = { onSelect(order) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = orders.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.primary,
                    activeBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    inactiveBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                ),
            ) {
                Text(text = stringResource(order.labelRes), fontSize = 14.sp)
            }
        }
    }
}

@Preview(name = "淺色")
@Preview(name = "深色", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StockQuerySheetPreview() {
    StockTheme {
        StockQuerySheet(
            query = StockQuery(trend = TrendFilter.UP, type = SecurityType.STOCK),
            resultCount = 312,
            onSortFieldSelect = {},
            onSortOrderSelect = {},
            onTrendToggle = {},
            onAverageToggle = {},
            onTypeToggle = {},
            onReset = {},
        )
    }
}

@Preview(name = "橫向（左右兩欄）", widthDp = 640, heightDp = 360)
@Composable
private fun StockQuerySheetTwoPanePreview() {
    StockTheme {
        StockQuerySheet(
            query = StockQuery(average = AverageFilter.ABOVE),
            resultCount = 512,
            onSortFieldSelect = {},
            onSortOrderSelect = {},
            onTrendToggle = {},
            onAverageToggle = {},
            onTypeToggle = {},
            onReset = {},
        )
    }
}
