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
import com.sebastienbalard.bicycle.BICTestApplication
import com.sebastienbalard.bicycle.SBCrashReport
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.data.BICContractDao
import com.sebastienbalard.bicycle.io.BicycleDataSource
import com.sebastienbalard.bicycle.io.CityBikesDataSource
import com.sebastienbalard.bicycle.io.dtos.BICContractsDataResponseDto
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class)
class BICContractRepositoryTester {

    @Rule
    @JvmField
    val execRule = InstantTaskExecutorRule()

    private val mockBicycleDataSource = mock(BicycleDataSource::class.java)
    private val mockCityBikesDataSource = mock(CityBikesDataSource::class.java)
    private val mockContractDao = mock(BICContractDao::class.java)
    private val mockPreferenceRepository = mock(BICPreferenceRepository::class.java)
    private val mockCrashReport = mock(SBCrashReport::class.java)

    private var repository: BICContractRepository? = null

    @Test
    fun testInit() {
        assertThat(repository, notNullValue())
    }

    @Test
    fun testUpdateContractsWithNewVersion() {

        val contract = BICContract(1, "Toulouse", 0.0, 0.0, "FR", 10,3000.0, "https://")
        val newContract1 = BICContract(1, "Toulouse", 0.0, 0.0, "FR", 10, 5000.0, "https://")
        val newContract2 = BICContract(2, "Paris", 0.0, 0.0, "FR", 10, 10000.0, "https://")
        val response = BICContractsDataResponseDto(2, arrayListOf(newContract1, newContract2))

        `when`(mockBicycleDataSource.getContracts()).thenReturn(Single.just(response))
        `when`(mockPreferenceRepository.contractsVersion).thenReturn(1)
        `when`(mockContractDao.findAll()).thenReturn(arrayListOf(contract))
        `when`(mockContractDao.findAll()).thenReturn(arrayListOf(newContract1, newContract2))

        val spy = repository!!.updateContracts(true).test().await()

        verify(mockBicycleDataSource).getContracts()
        verify(mockContractDao, times(2)).findAll()

        spy.assertValueCount(1)
        spy.assertValue(2)
    }

    @Test
    fun testUpdateContractsWithSameVersion() {

        val contract = BICContract(1, "Toulouse", 0.0, 0.0, "FR", 10, 3000.0, "https://")
        val response = BICContractsDataResponseDto(1, arrayListOf(contract))

        `when`(mockBicycleDataSource.getContracts()).thenReturn(Single.just(response))
        `when`(mockPreferenceRepository.contractsVersion).thenReturn(1)
        `when`(mockContractDao.findAll()).thenReturn(arrayListOf(contract))

        val spy = repository!!.updateContracts(true).test().await()

        verify(mockBicycleDataSource).getContracts()
        verify(mockContractDao).findAll()

        spy.assertValueCount(1)
        spy.assertValue(1)
    }

    @Test
    fun testGetContractCount() {

        val newContract1 = BICContract(1, "Toulouse", 0.0, 0.0, "FR", 10, 5000.0, "https://")
        val newContract2 = BICContract(2, "Paris", 0.0, 0.0, "FR", 10, 10000.0, "https://")

        `when`(mockContractDao.findAll()).thenReturn(arrayListOf(newContract1, newContract2))

        val spy = repository!!.getContractCount().test().await()

        verify(mockContractDao).findAll()

        spy.assertValueCount(1)
        spy.assertValue(2)
    }

    @Before
    fun setUp() {
        repository = BICContractRepository(application.applicationContext, mockBicycleDataSource, mockCityBikesDataSource, mockContractDao, mockPreferenceRepository, mockCrashReport)
    }

    @After
    fun tearDown() {
        repository = null
    }

    companion object {
        @BeforeClass
        fun setUpOnce() {
            RxAndroidPlugins.reset()
            RxJavaPlugins.reset()
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
            RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        }

        @AfterClass
        fun tearDownOnce() {
            RxAndroidPlugins.reset()
            RxJavaPlugins.reset()
        }
    }
}