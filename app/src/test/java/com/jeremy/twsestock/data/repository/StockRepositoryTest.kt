package com.jeremy.twsestock.data.repository

import com.jeremy.twsestock.data.mapper.StockMapper
import com.jeremy.twsestock.testutil.FakeTwseApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-10-13T09:20:35Z"), ZoneId.of("Asia/Taipei"))

    private fun createRepository(api: FakeTwseApi) = StockRepository(
        api = api,
        mapper = StockMapper(),
        defaultDispatcher = UnconfinedTestDispatcher(),
        clock = fixedClock,
    )

    /**
     * 合併三支 API 的資料，並記錄取得資料的時間。
     *
     * 測試對象：[StockRepository.getMarketSnapshot]
     */
    @Test
    fun getMarketSnapshot_success_mergesDataAndRecordsFetchTime() = runTest {
        val snapshot = createRepository(FakeTwseApi()).getMarketSnapshot()
        val codes = snapshot.stocks.map { it.code }

        println("交易日期：期待 2026-10-08，實際 ${snapshot.tradeDate}")
        assertEquals(LocalDate.of(2026, 10, 8), snapshot.tradeDate)
        println("取得時間（注入的 Clock）：期待 2026-10-13T17:20:35，實際 ${snapshot.fetchedAt}")
        assertEquals(LocalDateTime.of(2026, 10, 13, 17, 20, 35), snapshot.fetchedAt)
        println("個股代號：期待 [2330, 2317, 0050]，實際 $codes")
        assertEquals(listOf("2330", "2317", "0050"), codes)
    }

    /**
     * API 失敗時把例外往上拋，交給 ViewModel 處理。
     *
     * 測試對象：[StockRepository.getMarketSnapshot]
     */
    @Test
    fun getMarketSnapshot_apiFails_throwsException() = runTest {
        val error = runCatching {
            createRepository(FakeTwseApi(error = IOException("network"))).getMarketSnapshot()
        }.exceptionOrNull()

        println("拋出的例外：期待 IOException，實際 ${error?.let { it::class.simpleName }}")
        assertEquals(IOException::class, error?.let { it::class })
    }
}
