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

import android.arch.persistence.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sebastienbalard.bicycle.BICApplication
import com.sebastienbalard.bicycle.BuildConfig
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.data.BICDatabase
import com.sebastienbalard.bicycle.io.BicycleApi
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.applicationContext
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val localDataSourceModule = module {

    bean {
        Room.databaseBuilder(androidContext(), BICDatabase::class.java, BuildConfig.ROOM_DB_NAME).build()
    }
    bean { get<BICDatabase>().getContractDao() }
}