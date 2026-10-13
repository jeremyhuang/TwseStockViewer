package com.jeremy.twsestock.di

import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import java.time.Clock

class KoinModulesTest {

    /** 任何 Koin 定義缺少依賴時，這個測試會失敗，不用等到 App 執行才發現。 */
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `所有 Koin 模組的依賴都能被滿足`() {
        // StockRepository 的 dispatcher 與 clock 有預設值、不由 Koin 提供，宣告為額外型別略過檢查。
        module { includes(appModule, networkModule) }.verify(
            extraTypes = listOf(CoroutineDispatcher::class, Clock::class),
        )
    }
}
