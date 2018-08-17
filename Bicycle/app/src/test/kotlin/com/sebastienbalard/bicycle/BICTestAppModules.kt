package com.sebastienbalard.bicycle

import com.sebastienbalard.bicycle.viewmodels.BICSplashViewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.mockito.Mockito.mock

val commonTestModule = module {

    viewModel { mock(BICSplashViewModel::class.java) }
}