package com.jeremy.twsestock.ui.query

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.jeremy.twsestock.R
import com.jeremy.twsestock.data.model.PriceTrend
import com.jeremy.twsestock.ui.list.StockUiModel

/** 有顯示名稱的選項，排序與篩選面板用它產生 chip 的文字。 */
interface LabeledOption {
    /** 選項的顯示名稱。 */
    @get:StringRes
    val labelRes: Int
}

/** 排序欄位。 */
enum class SortField(@get:StringRes override val labelRes: Int) : LabeledOption {
    CODE(R.string.sort_field_code),
    CLOSING_PRICE(R.string.sort_field_closing_price),
    CHANGE(R.string.sort_field_change),
    CHANGE_PERCENT(R.string.sort_field_change_percent),
    TRADE_VOLUME(R.string.sort_field_trade_volume),
    TRADE_VALUE(R.string.sort_field_trade_value),
    PE_RATIO(R.string.sort_field_pe_ratio),
    DIVIDEND_YIELD(R.string.sort_field_dividend_yield),
    PB_RATIO(R.string.sort_field_pb_ratio),
}

/**
 * 排序方向。
 *
 * @property shortLabelRes 主畫面排序 chip 上的簡短文字，例如「降序」。
 * @property iconRes 主畫面排序 chip 上的方向箭頭。
 */
enum class SortOrder(
    @get:StringRes override val labelRes: Int,
    @StringRes val shortLabelRes: Int,
    @DrawableRes val iconRes: Int,
) : LabeledOption {
    DESCENDING(R.string.sort_order_descending, R.string.sort_order_descending_short, R.drawable.ic_arrow_downward),
    ASCENDING(R.string.sort_order_ascending, R.string.sort_order_ascending_short, R.drawable.ic_arrow_upward),
}

/** 今日漲跌篩選。 */
enum class TrendFilter(@get:StringRes override val labelRes: Int) : LabeledOption {
    UP(R.string.filter_trend_up),
    DOWN(R.string.filter_trend_down),
    FLAT(R.string.filter_trend_flat),
    ;

    /**
     * 判斷個股是否符合條件。
     *
     * 平盤只算「有收盤價且漲跌為 0」的個股；今天沒有成交的個股漲跌也是 null，不能算成平盤。
     *
     * @param stock 要判斷的個股。
     * @return 符合時為 true。
     */
    fun matches(stock: StockUiModel): Boolean = when (this) {
        UP -> stock.changeTrend == PriceTrend.UP
        DOWN -> stock.changeTrend == PriceTrend.DOWN
        FLAT -> stock.changeTrend == PriceTrend.FLAT && stock.change != null && stock.closingPrice != null
    }
}

/** 收盤價與月平均價比較的篩選。 */
enum class AverageFilter(@get:StringRes override val labelRes: Int) : LabeledOption {
    ABOVE(R.string.filter_average_above),
    BELOW(R.string.filter_average_below),
    ;

    /**
     * 判斷個股是否符合條件。沒有月平均價的個股兩個條件都不符合。
     *
     * @param stock 要判斷的個股。
     * @return 符合時為 true。
     */
    fun matches(stock: StockUiModel): Boolean = when (this) {
        ABOVE -> stock.closingTrend == PriceTrend.UP
        BELOW -> stock.closingTrend == PriceTrend.DOWN
    }
}

/**
 * 證券類型篩選。
 *
 * 證交所沒有提供類型欄位，依代號的慣例判斷：4 碼數字且非 0 開頭為個股，00 開頭為 ETF。
 * ETN、特別股、TDR 等其他證券兩個條件都不符合。
 */
enum class SecurityType(@get:StringRes override val labelRes: Int) : LabeledOption {
    STOCK(R.string.filter_type_stock),
    ETF(R.string.filter_type_etf),
    ;

    /**
     * 判斷個股是否符合條件。
     *
     * @param stock 要判斷的個股。
     * @return 符合時為 true。
     */
    fun matches(stock: StockUiModel): Boolean = when (this) {
        STOCK -> STOCK_CODE.matches(stock.code)
        ETF -> stock.code.startsWith(ETF_PREFIX)
    }

    private companion object {
        /** 一般個股代號：4 碼數字，第一碼不是 0。 */
        val STOCK_CODE = Regex("[1-9]\\d{3}")

        /** ETF 代號的開頭。 */
        const val ETF_PREFIX = "00"
    }
}

/**
 * 列表的排序與篩選條件。
 *
 * 三組篩選各自單選，null 代表不篩選；不同組之間是 AND。
 */
data class StockQuery(
    val sortField: SortField = SortField.TRADE_VOLUME,   // 排序欄位，預設成交量
    val sortOrder: SortOrder = SortOrder.DESCENDING,     // 排序方向，預設降序
    val trend: TrendFilter? = null,                      // 今日漲跌
    val average: AverageFilter? = null,                  // 收盤價 vs 月均價
    val type: SecurityType? = null,                      // 證券類型
) {
    /** 是否有任何篩選條件（不含排序）。 */
    val hasFilter: Boolean
        get() = trend != null || average != null || type != null

    /** 目前有選的篩選條件，依面板上的順序排列。 */
    val activeFilters: List<LabeledOption>
        get() = listOfNotNull(trend, average, type)

    /**
     * 切換今日漲跌篩選：選已選的就取消，否則改選這個。
     *
     * @param filter 被點選的條件。
     * @return 新的查詢條件。
     */
    fun toggled(filter: TrendFilter): StockQuery = copy(trend = filter.takeIf { it != trend })

    /**
     * 切換月均價篩選：選已選的就取消，否則改選這個。
     *
     * @param filter 被點選的條件。
     * @return 新的查詢條件。
     */
    fun toggled(filter: AverageFilter): StockQuery = copy(average = filter.takeIf { it != average })

    /**
     * 切換證券類型篩選：選已選的就取消，否則改選這個。
     *
     * @param filter 被點選的條件。
     * @return 新的查詢條件。
     */
    fun toggled(filter: SecurityType): StockQuery = copy(type = filter.takeIf { it != type })

    /**
     * 清除所有篩選條件，保留排序。
     *
     * @return 新的查詢條件。
     */
    fun withoutFilters(): StockQuery = copy(trend = null, average = null, type = null)

    /**
     * 依目前條件篩選並排序個股。
     *
     * 排序欄位沒有資料（null）的個股不論升降序都排在最後；數值相同時再依代號排序，讓結果穩定。
     *
     * @param stocks 所有個股。
     * @return 篩選、排序後的個股。
     */
    fun filterAndSort(stocks: List<StockUiModel>): List<StockUiModel> =
        stocks
            .filter { stock ->
                trend?.matches(stock) != false &&
                    average?.matches(stock) != false &&
                    type?.matches(stock) != false
            }
            .sortedWith(comparator().thenBy { it.code })

    /** 依排序欄位與方向建立比較器。 */
    private fun comparator(): Comparator<StockUiModel> = when (sortField) {
        SortField.CODE -> sortBy { it.code }
        SortField.CLOSING_PRICE -> sortBy { it.closingPrice }
        SortField.CHANGE -> sortBy { it.change }
        SortField.CHANGE_PERCENT -> sortBy { it.changePercent }
        SortField.TRADE_VOLUME -> sortBy { it.tradeVolume }
        SortField.TRADE_VALUE -> sortBy { it.tradeValue }
        SortField.PE_RATIO -> sortBy { it.peRatio }
        SortField.DIVIDEND_YIELD -> sortBy { it.dividendYield }
        SortField.PB_RATIO -> sortBy { it.pbRatio }
    }

    /**
     * 建立單一欄位的比較器，null 一律排最後。
     *
     * @param selector 取出要比較的欄位。
     * @return 依 [sortOrder] 排序的比較器。
     */
    private fun <T : Comparable<T>> sortBy(selector: (StockUiModel) -> T?): Comparator<StockUiModel> {
        val valueOrder: Comparator<T> = when (sortOrder) {
            SortOrder.DESCENDING -> reverseOrder()
            SortOrder.ASCENDING -> naturalOrder()
        }
        return compareBy<StockUiModel, T?>(nullsLast(valueOrder), selector)
    }
}
