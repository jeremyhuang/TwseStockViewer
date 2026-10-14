package com.jeremy.twsestock.di

import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import java.time.Clock

class KoinModulesTest {

    /**
     * 任何 Koin 定義缺少依賴時，這個測試會失敗，不用等到 App 執行才發現。
     *
     * 測試對象：[appModule]、[networkModule]
     */
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun allModules_dependenciesAreSatisfied() {
        println("檢查 appModule + networkModule 的依賴（缺少時 verify 會拋出例外並列出缺少的型別）")
        // StockRepository 的 dispatcher 與 clock 有預設值、不由 Koin 提供，宣告為額外型別略過檢查。
        module { includes(appModule, networkModule) }.verify(
            extraTypes = listOf(CoroutineDispatcher::class, Clock::class),
        )
        println("所有依賴都能滿足")
    }
}
