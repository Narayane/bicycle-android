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

package com.sebastienbalard.bicycle.repositories

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.SharedPreferences
import com.sebastienbalard.bicycle.BICTestApplication
import com.sebastienbalard.bicycle.BuildConfig
import com.sebastienbalard.bicycle.io.BicycleDataSource
import com.sebastienbalard.bicycle.io.dtos.BICConfigAndroidDto
import com.sebastienbalard.bicycle.io.dtos.BICConfigAppsDto
import com.sebastienbalard.bicycle.io.dtos.BICConfigContractsDto
import com.sebastienbalard.bicycle.io.dtos.BICConfigResponseDto
import io.reactivex.Single
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class, constants = BuildConfig::class)
class BICPreferenceRepositoryTester {

    @Rule
    @JvmField
    val execRule = InstantTaskExecutorRule()

    private val mockBicycleDataSource = mock(BicycleDataSource::class.java)
    private val mockSharedPreferences = mock(SharedPreferences::class.java)

    private var repository: BICPreferenceRepository? = null

    @Test
    fun testInit() {
        assertThat(repository, notNullValue())
    }

    @Test
    fun testLoadConfig() {

        val response = BICConfigResponseDto(BICConfigAppsDto(14, BICConfigAndroidDto("1.0", false)), BICConfigContractsDto(30))

        `when`(mockBicycleDataSource.getConfig()).thenReturn(Single.just(response))

        val spy = repository!!.loadConfig().test()

        verify(mockBicycleDataSource).getConfig()

        //Assert.assertThat(repository!!.appCheckDelay, `is`(equalTo(14)))
        //Assert.assertThat(repository!!.contractsCheckDelay, `is`(equalTo(30)))
    }

    @Before
    fun setUp() {
        repository = BICPreferenceRepository(mockBicycleDataSource, mockSharedPreferences)
    }

    @After
    fun tearDown() {
        repository = null
    }
}