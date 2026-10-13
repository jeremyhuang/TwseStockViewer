package com.jeremy.twsestock.di

import com.jeremy.twsestock.data.mapper.StockMapper
import com.jeremy.twsestock.data.repository.StockRepository
import com.jeremy.twsestock.ui.list.StockListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** App 的 Koin 依賴設定（資料層與 ViewModel）。網路相關設定在 [networkModule]。 */
val appModule = module {
    factoryOf(::StockMapper)

    single { StockRepository(api = get(), mapper = get()) }

    viewModelOf(::StockListViewModel)
}
