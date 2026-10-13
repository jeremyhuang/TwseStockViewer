package com.jeremy.twsestock.di

import com.jeremy.twsestock.ui.list.StockListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::StockListViewModel)
}
