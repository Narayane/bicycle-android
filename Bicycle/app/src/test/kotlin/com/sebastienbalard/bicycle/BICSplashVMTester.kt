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
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import com.sebastienbalard.bicycle.viewmodels.*
import io.reactivex.Completable
import io.reactivex.Single
import org.hamcrest.CoreMatchers.*
import org.joda.time.DateTime
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class, constants = BuildConfig::class)
class BICSplashVMTester {

    @Rule
    @JvmField
    val execRule = InstantTaskExecutorRule()

    private val mockPreferenceRepository = mock(BICPreferenceRepository::class.java)
    private val mockContractRepository = mock(BICContractRepository::class.java)

    private var viewModel: BICSplashViewModel? = null

    @Test
    fun testInit() {
        assertThat(viewModel, notNullValue())
        assertThat(viewModel!!.states, notNullValue())
        assertThat(viewModel!!.events, notNullValue())
    }

    @Test
    fun testLoadConfig() {

        `when`(mockPreferenceRepository.loadConfig()).thenReturn(Completable.complete())

        viewModel!!.loadConfig()

        verify(mockPreferenceRepository).loadConfig()

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateSplashConfig::class.java))

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventSplashConfigLoaded::class.java))
    }

    @Test
    fun testLoadConfigWithError() {

        `when`(mockPreferenceRepository.loadConfig()).thenReturn(Completable.error(RuntimeException()))

        viewModel!!.loadConfig()

        verify(mockPreferenceRepository).loadConfig()

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateSplashConfig::class.java))

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventSplashLoadConfigFailed::class.java))
    }

    @Test
    fun testLoadContractsForFirstTime() {

        val events = arrayListOf<Event>()
        viewModel!!.events.observeForever {
            it?.let {
                events.add(it)
            }
        }

        `when`(mockPreferenceRepository.contractsLastCheckDate).thenReturn(null)
        `when`(mockContractRepository.updateContracts()).thenReturn(Single.just(523))

        viewModel!!.loadAllContracts()

        verify(mockPreferenceRepository).contractsLastCheckDate
        verify(mockContractRepository).updateContracts()

        assertThat(events.size, `is`(equalTo(2)))

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateSplashContracts::class.java))

        assertThat(events, notNullValue())
        assertThat(events[0], instanceOf(EventSplashCheckContracts::class.java))
        assertThat(events[1], instanceOf(EventSplashAvailableContracts::class.java))

        val event = events[1] as EventSplashAvailableContracts

        assertThat(event.count, `is`(equalTo(523)))
    }

    @Test
    fun testLoadContractsWhenNotNecessary() {

        val events = arrayListOf<Event>()
        viewModel!!.events.observeForever {
            it?.let {
                events.add(it)
            }
        }

        `when`(mockPreferenceRepository.contractsLastCheckDate).thenReturn(DateTime.now().minusDays(10))
        `when`(mockContractRepository.getContractCount()).thenReturn(Single.just(255))

        viewModel!!.loadAllContracts()

        verify(mockPreferenceRepository).contractsLastCheckDate
        verify(mockContractRepository).getContractCount()

        assertThat(events.size, `is`(equalTo(1)))

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateSplashContracts::class.java))

        assertThat(events, notNullValue())
        assertThat(events[0], instanceOf(EventSplashAvailableContracts::class.java))

        val event = events[0] as EventSplashAvailableContracts

        assertThat(event.count, `is`(equalTo(255)))
    }

    @Test
    fun testLoadContractsWhenNecessary() {

        val events = arrayListOf<Event>()
        viewModel!!.events.observeForever {
            it?.let {
                events.add(it)
            }
        }

        `when`(mockPreferenceRepository.contractsLastCheckDate).thenReturn(DateTime.now().minusDays(BuildConfig.DAYS_BETWEEN_CONTRACTS_CHECK + 1))
        `when`(mockContractRepository.updateContracts()).thenReturn(Single.just(608))

        viewModel!!.loadAllContracts()

        verify(mockPreferenceRepository).contractsLastCheckDate
        verify(mockContractRepository).updateContracts()

        assertThat(events.size, `is`(equalTo(2)))

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateSplashContracts::class.java))

        assertThat(events, notNullValue())
        assertThat(events[0], instanceOf(EventSplashCheckContracts::class.java))
        assertThat(events[1], instanceOf(EventSplashAvailableContracts::class.java))

        val event = events[1] as EventSplashAvailableContracts

        assertThat(event.count, `is`(equalTo(608)))
    }

    @Before
    fun setUp() {
        viewModel = BICSplashViewModel(RuntimeEnvironment.application.applicationContext, mockPreferenceRepository, mockContractRepository)
    }

    @After
    fun tearDown() {
        viewModel = null
    }
}
