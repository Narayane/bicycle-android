/**
 * Copyright © 2018 Bicycle (Sébastien BALARD)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebastienbalard.bicycle.di

import android.preference.PreferenceManager
import com.sebastienbalard.bicycle.SBMapViewModel
import com.sebastienbalard.bicycle.io.BicycleDataSource
import com.sebastienbalard.bicycle.io.CityBikesDataSource
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import com.sebastienbalard.bicycle.viewmodels.BICHomeViewModel
import com.sebastienbalard.bicycle.viewmodels.BICSplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val commonModule = module {

    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
    single { BicycleDataSource(get()) }
    single { CityBikesDataSource(get()) }
    single { BICPreferenceRepository(get(), get()) }
    single { BICContractRepository(get(), get(), get(), get()) }

    viewModel { SBMapViewModel(get()) }
}

val homeModule = module {

    viewModel { BICSplashViewModel(get(), get(), get()) }
    viewModel { BICHomeViewModel(get()) }
}

val bicycleApp = listOf(remoteDataSourceModule, localDataSourceModule, commonModule, homeModule)