package com.jeremy.twsestock.data.repository

import com.jeremy.twsestock.data.mapper.StockMapper
import com.jeremy.twsestock.data.model.MarketSnapshot
import com.jeremy.twsestock.data.remote.TwseApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDateTime

/**
 * 上市個股資料的來源：從證交所 OpenAPI 取得資料。
 *
 * @param api 證交所 API。
 * @param mapper 將三支 API 的回應合併成 [MarketSnapshot]。
 * @param defaultDispatcher 執行合併運算的 dispatcher。
 * @param clock 取得目前時間的時鐘；測試時可替換成固定時間。
 */
class StockRepository(
    private val api: TwseApi,
    private val mapper: StockMapper,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    /**
     * 取得最新交易日的所有上市個股資料。
     *
     * @return 交易日期、資料取得時間與所有個股。
     * @throws Exception 網路或資料解析失敗時拋出。
     */
    suspend fun getMarketSnapshot(): MarketSnapshot =
        coroutineScope {
            val dailyTrading = async { api.getDailyTrading() }
            val monthlyAverages = async { api.getMonthlyAverages() }
            val valuations = async { api.getValuations() }

            withContext(defaultDispatcher) {
                mapper.map(
                    dailyTrading = dailyTrading.await(),
                    monthlyAverages = monthlyAverages.await(),
                    valuations = valuations.await(),
                    fetchedAt = LocalDateTime.now(clock)
                )
            }
        }
}
