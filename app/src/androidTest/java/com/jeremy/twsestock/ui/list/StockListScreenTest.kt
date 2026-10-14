package com.jeremy.twsestock.ui.list

import android.app.KeyguardManager
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jeremy.twsestock.MainActivity
import com.jeremy.twsestock.R
import com.jeremy.twsestock.data.mapper.StockMapper
import com.jeremy.twsestock.data.repository.StockRepository
import com.jeremy.twsestock.ui.query.StockQuerySheetFragment
import kotlinx.coroutines.Dispatchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 股票列表畫面的 UI 測試：列表顯示、估值彈窗、排序與篩選。
 *
 * 列表與 Bottom Sheet 是 XML（用 Espresso 操作），彈窗與面板內容是 Compose（用 Compose 測試 API 操作）。
 */
@RunWith(AndroidJUnit4::class)
class StockListScreenTest {

    // 不自動開啟 Activity：要先把 Koin 的 Repository 換成假資料，再手動開啟。
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    /** 這次測試載入的假資料模組，測試結束時卸載。 */
    private lateinit var testModule: Module

    private fun string(@StringRes resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    /**
     * 把 Koin 的 Repository 換成假 API 後開啟畫面。
     *
     * 合併運算放在主執行緒，讓 Espresso 能等資料載入完成再檢查畫面。
     *
     * @param api 測試用的 API，預設為三檔股票。
     */
    private fun launch(api: FakeTwseApi = FakeTwseApi()) {
        testModule = module {
            single {
                StockRepository(
                    api = api,
                    mapper = StockMapper(),
                    defaultDispatcher = Dispatchers.Main.immediate,
                )
            }
        }
        loadKoinModules(testModule)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        // 模擬器螢幕關閉或鎖定時，畫面拿不到焦點，Espresso 會一直等到逾時；這裡讓測試時自動亮屏並略過鎖定畫面。
        scenario.onActivity { activity ->
            activity.setShowWhenLocked(true)
            activity.setTurnScreenOn(true)
            activity.getSystemService(KeyguardManager::class.java).requestDismissKeyguard(activity, null)
        }
    }

    /**
     * 取得列表目前顯示的股票代號（依畫面上的順序）。
     *
     * @return 列表裡所有股票的代號。
     */
    private fun listedCodes(): List<String> {
        var codes = emptyList<String>()
        onView(withId(R.id.rv_stock_list)).check { view, _ ->
            codes = ((view as RecyclerView).adapter as StockAdapter).currentList.map { it.code }
        }
        return codes
    }

    /**
     * 關閉排序與篩選面板。
     *
     * 不用 pressBack()：面板剛打開、還沒拿到焦點時按返回鍵，返回鍵會送到 Activity，
     * 結果關掉的是整個 App（畫面滑走），後面的檢查就會因為找不到有焦點的畫面而逾時。
     * 這裡直接找到面板的 Fragment 把它關掉，不受時序影響。
     */
    private fun closeQuerySheet() {
        scenario.onActivity { activity ->
            activity.supportFragmentManager.findFragmentById(R.id.fcv_main)
                ?.childFragmentManager
                ?.findFragmentByTag(StockQuerySheetFragment.TAG)
                ?.let { it as DialogFragment }
                ?.dismissNow()
        }
    }

    /**
     * 從列表中間往上滑（列表往下捲）。
     *
     * 不用 Espresso 內建的 swipeUp()：它從 View 的最底部開始滑，列表延伸到螢幕底部（edge-to-edge），
     * 起點會落在手勢導覽列，系統當成「回到桌面」手勢，App 就被切到背景了。
     */
    private fun scrollListDown() = GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER, GeneralLocation.TOP_CENTER, Press.FINGER)

    /** 從列表中間往下滑（列表往回捲），同樣避開螢幕邊緣的系統手勢區。 */
    private fun scrollListUp() = GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER, GeneralLocation.BOTTOM_CENTER, Press.FINGER)

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        // 卸載這次的假資料，下一個測試重新載入，避免沿用上一個測試建立的 Repository（例如只有 3 檔的版本）。
        if (::testModule.isInitialized) unloadKoinModules(testModule)
    }

    /**
     * 列表顯示 API 回傳的個股。
     *
     * 測試對象：StockListFragment 列表顯示（StockAdapter 綁定）
     */
    @Test
    fun stockList_showsStocksFromApi() {
        launch()
        println("列表顯示「台積電」")
        onView(withText("台積電")).check(matches(isDisplayed()))
        println("列表顯示「鴻海」")
        onView(withText("鴻海")).check(matches(isDisplayed()))
        println("列表顯示「元大台灣50」")
        onView(withText("元大台灣50")).check(matches(isDisplayed()))
    }

    /**
     * 點選卡片顯示估值彈窗，按確定後關閉。
     *
     * 測試對象：StockAdapter 點擊 → StockListFragment.showStockInfo → [com.jeremy.twsestock.ui.info.StockInfoDialogFragment]
     */
    @Test
    fun clickStockCard_showsValuationDialog_closesOnConfirm() {
        launch()
        println("點選「台積電」卡片")
        onView(withText("台積電")).perform(click())
        println("彈窗顯示「${string(R.string.label_pe_ratio)}」標籤")
        composeRule.onNodeWithText(string(R.string.label_pe_ratio)).assertIsDisplayed()
        println("本益比顯示 25.12")
        composeRule.onNodeWithText("25.12").assertIsDisplayed()
        println("殖利率顯示 1.52")
        composeRule.onNodeWithText("1.52").assertIsDisplayed()
        println("股價淨值比顯示 7.45")
        composeRule.onNodeWithText("7.45").assertIsDisplayed()
        println("點選「${string(R.string.action_confirm)}」")
        composeRule.onNodeWithText(string(R.string.action_confirm)).performClick()
        println("彈窗已關閉（25.12 不存在）")
        composeRule.onNodeWithText("25.12").assertDoesNotExist()
    }

    /**
     * 篩選上漲後只顯示上漲的個股，並在標題列下方顯示條件。
     *
     * 測試對象：[com.jeremy.twsestock.ui.query.StockQuerySheetFragment] → [StockListViewModel.onTrendToggle] → StockListFragment.renderQueryChips
     */
    @Test
    fun filterRising_showsOnlyRisingStocksAndActiveChip() {
        launch()
        val up = string(R.string.filter_trend_up)
        println("點選右上角排序與篩選")
        onView(withId(R.id.action_query)).perform(click())
        println("面板中點選「$up」")
        composeRule.onNodeWithText(up).performClick()
        println("關閉面板")
        closeQuerySheet()
        println("列表顯示上漲的「台積電」")
        onView(withText("台積電")).check(matches(isDisplayed()))
        println("列表顯示上漲的「元大台灣50」")
        onView(withText("元大台灣50")).check(matches(isDisplayed()))
        println("列表不顯示下跌的「鴻海」")
        onView(withText("鴻海")).check(doesNotExist())
        println("標題列下方顯示「$up」條件 chip")
        onView(allOf(withText(up), isDescendantOfA(withId(R.id.cg_query)))).check(matches(isDisplayed()))
    }

    /**
     * 篩選後沒有結果時顯示空狀態，清除篩選後恢復列表。
     *
     * 測試對象：StockQuerySheetFragment → [StockListUiState.isFilteredEmpty] 空狀態 → [StockListViewModel.onFiltersClear]
     */
    @Test
    fun filterWithNoResult_showsEmptyState_clearRestoresList() {
        launch()
        val etf = string(R.string.filter_type_etf)
        val down = string(R.string.filter_trend_down)
        println("點選右上角排序與篩選")
        onView(withId(R.id.action_query)).perform(click())
        println("面板中點選「$etf」")
        composeRule.onNodeWithText(etf).performClick()
        println("面板中點選「$down」")
        composeRule.onNodeWithText(down).performClick()
        println("關閉面板")
        closeQuerySheet()
        println("顯示空狀態")
        onView(withId(R.id.ll_empty)).check(matches(isDisplayed()))
        println("點選「${string(R.string.action_clear_filter)}」")
        onView(withId(R.id.btn_clear_filter)).perform(click())
        println("列表恢復，顯示「鴻海」")
        onView(withText("鴻海")).check(matches(isDisplayed()))
    }

    /**
     * 點掉標題列下方的篩選條件 chip 後，該條件被移除，列表恢復並依原本的排序重新排列。
     *
     * 測試對象：StockListFragment.renderQueryChips → removeFilter → [StockListViewModel.onTrendToggle]
     */
    @Test
    fun removeFilterChip_restoresListInSortedOrder() {
        launch()
        val up = string(R.string.filter_trend_up)
        val upChip = allOf(withText(up), isDescendantOfA(withId(R.id.cg_query)))

        println("點選右上角排序與篩選，選「$up」後關閉面板")
        onView(withId(R.id.action_query)).perform(click())
        composeRule.onNodeWithText(up).performClick()
        closeQuerySheet()

        val filtered = listedCodes()
        println("篩選上漲後的列表：期待 [2330, 0050]，實際 $filtered")
        assertEquals(listOf("2330", "0050"), filtered)

        println("點掉標題列下方的「$up」chip")
        onView(upChip).perform(click())

        println("「$up」chip 已移除")
        onView(upChip).check(doesNotExist())
        val restored = listedCodes()
        println("移除篩選後的列表（成交量降序）：期待 [2317, 2330, 0050]，實際 $restored")
        assertEquals(listOf("2317", "2330", "0050"), restored)
    }

    /**
     * 列表往下捲時標題列收起、條件 chip 固定在上方；往回捲時標題列重新出現。
     *
     * 測試對象：fragment_stock_list.xml 的 AppBarLayout（tb_stock_list 的 scrollFlags = scroll|enterAlways|snap，hsv_query 無 scrollFlags）
     */
    @Test
    fun scrollList_collapsesToolbarAndKeepsQueryChips() {
        launch(FakeTwseApi(fillerCount = 30))

        println("一開始標題列完整顯示")
        onView(withId(R.id.tb_stock_list)).check(matches(isCompletelyDisplayed()))
        println("一開始條件 chip 顯示")
        onView(withId(R.id.hsv_query)).check(matches(isDisplayed()))

        println("列表往下捲")
        onView(withId(R.id.rv_stock_list)).perform(scrollListDown())

        println("標題列已收起（不在畫面上）")
        onView(withId(R.id.tb_stock_list)).check(matches(not(isDisplayed())))
        println("條件 chip 仍固定在上方")
        onView(withId(R.id.hsv_query)).check(matches(isDisplayed()))

        println("列表往回捲")
        onView(withId(R.id.rv_stock_list)).perform(scrollListUp())

        println("標題列重新完整顯示")
        onView(withId(R.id.tb_stock_list)).check(matches(isCompletelyDisplayed()))
    }
}
