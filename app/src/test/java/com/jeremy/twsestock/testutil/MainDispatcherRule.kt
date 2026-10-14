package com.jeremy.twsestock.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * 把 Dispatchers.Main 換成測試用的 dispatcher。
 *
 * JVM 單元測試沒有 Android 的主執行緒，viewModelScope 預設跑在 Main 上，不替換會直接拋例外。
 *
 * @property testDispatcher 取代 Main 的 dispatcher；預設為 Unconfined，協程會立即執行，不用手動推進時間。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
