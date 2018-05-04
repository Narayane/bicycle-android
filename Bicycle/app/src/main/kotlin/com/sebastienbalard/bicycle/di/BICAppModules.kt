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

import com.sebastienbalard.bicycle.repositories.BICContractRepository
import com.sebastienbalard.bicycle.viewmodels.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val commonModule = applicationContext {

    bean { BICContractRepository(get(), get()) }

    viewModel { BICMapViewModel(get()) }
    viewModel { BICSplashViewModel(get()) }
}

val homeModule = applicationContext {

    viewModel { BICHomeViewModel(get()) }
    viewModel { BICSearchViewModel(get()) }
}

val rideModule = applicationContext {

    this
    viewModel { BICRideViewModel(get()) }
}

val bicycleApp = listOf(remoteDataSourceModule, localDataSourceModule, commonModule, homeModule, rideModule)