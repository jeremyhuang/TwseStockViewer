package com.jeremy.twsestock.di

import com.jeremy.twsestock.data.repository.StockRepository
import com.jeremy.twsestock.ui.list.StockListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** App 的 Koin 依賴設定。 */
val appModule = module {
    // Repository 整個 App 共用一個實例，之後串接 API 時可在裡面做記憶體快取。
    singleOf(::StockRepository)

    viewModelOf(::StockListViewModel)
}
