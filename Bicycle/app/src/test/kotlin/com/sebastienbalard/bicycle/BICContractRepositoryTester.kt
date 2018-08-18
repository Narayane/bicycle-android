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

package com.sebastienbalard.bicycle

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.data.BICContractDao
import com.sebastienbalard.bicycle.io.BicycleApi
import com.sebastienbalard.bicycle.io.dtos.BICContractsDataResponseDto
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
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
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class, constants = BuildConfig::class)
class BICContractRepositoryTester {

    @Rule
    @JvmField
    val execRule = InstantTaskExecutorRule()

    private val mockBicycleApi = mock(BicycleApi::class.java)
    private val mockContractDao = mock(BICContractDao::class.java)
    private val mockPreferenceRepository = mock(BICPreferenceRepository::class.java)

    private var repository: BICContractRepository? = null

    @Test
    fun testInit() {
        assertThat(repository, notNullValue())
    }

    @Test
    fun testUpdateContractsWithNewVersion() {

        val contract = BICContract(1, "Toulouse", 0.0, 0.0, BICContract.Provider.CityBikes, 3000.0, "https://")
        val newContract1 = BICContract(1, "Toulouse", 0.0, 0.0, BICContract.Provider.CityBikes, 5000.0, "https://")
        val newContract2 = BICContract(2, "Paris", 0.0, 0.0, BICContract.Provider.CityBikes, 10000.0, "https://")
        val response = BICContractsDataResponseDto(2, arrayListOf(newContract1, newContract2))

        `when`(mockBicycleApi.getContractsData("media")).thenReturn(Single.just(response))
        `when`(mockPreferenceRepository.contractsVersion).thenReturn(1)
        `when`(mockContractDao.findAll()).thenReturn(arrayListOf(contract))
        `when`(mockContractDao.getAllCount()).thenReturn(2)

        val spy = repository!!.updateContracts().test().await()

        verify(mockBicycleApi).getContractsData("media")
        verify(mockContractDao).findAll()
        verify(mockContractDao).deleteAll(arrayListOf(contract))
        verify(mockContractDao).insertAll(arrayListOf(newContract1, newContract2))
        verify(mockContractDao).getAllCount()

        spy.assertValueCount(1)
        spy.assertValue(2)
    }

    @Test
    fun testUpdateContractsWithSameVersion() {

        val contract = BICContract(1, "Toulouse", 0.0, 0.0, BICContract.Provider.CityBikes, 3000.0, "https://")
        val response = BICContractsDataResponseDto(1, arrayListOf(contract))

        `when`(mockBicycleApi.getContractsData("media")).thenReturn(Single.just(response))
        `when`(mockPreferenceRepository.contractsVersion).thenReturn(1)
        `when`(mockContractDao.findAll()).thenReturn(arrayListOf(contract))
        `when`(mockContractDao.getAllCount()).thenReturn(1)

        val spy = repository!!.updateContracts().test().await()

        verify(mockBicycleApi).getContractsData("media")
        verify(mockContractDao).getAllCount()

        spy.assertValueCount(1)
        spy.assertValue(1)
    }

    @Test
    fun testGetContractCount() {

        `when`(mockContractDao.getAllCount()).thenReturn(2)

        val spy = repository!!.getContractCount().test().await()

        verify(mockContractDao).getAllCount()

        spy.assertValueCount(1)
        spy.assertValue(2)
    }

    @Before
    fun setUp() {
        repository = BICContractRepository(mockBicycleApi, mockContractDao, mockPreferenceRepository)
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