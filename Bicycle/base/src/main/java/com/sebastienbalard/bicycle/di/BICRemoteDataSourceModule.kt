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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.io.BicycleApi
import com.sebastienbalard.bicycle.io.CityBikesApi
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val remoteDataSourceModule = module {

    factory { initGson() }
    factory { initHttpClient() }
    factory { initApi<BicycleApi>(get(), get(), androidContext().getString(R.string.bicycle_storage_endpoint)) }
    factory { initApi<CityBikesApi>(get(), get(), "http://api.citybik.es/v2/networks/") }
}

fun initGson(): Gson {
    return GsonBuilder().serializeNulls().create()
}

fun initHttpClient(): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
    return OkHttpClient.Builder()
            .connectTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor).build()
}

inline fun <reified T> initApi(client: OkHttpClient, gson: Gson, url: String): T {
    val retrofit = Retrofit.Builder().client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .baseUrl(url)
            .build()
    return retrofit.create(T::class.java)
}