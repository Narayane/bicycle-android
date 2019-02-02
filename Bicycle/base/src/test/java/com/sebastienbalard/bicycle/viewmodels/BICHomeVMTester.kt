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

package com.sebastienbalard.bicycle.viewmodels

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.sebastienbalard.bicycle.BICTestApplication
import com.sebastienbalard.bicycle.EventFailure
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.Single
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeoutException

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class)
class BICHomeVMTester {

    @Rule
    @JvmField
    val execRule = InstantTaskExecutorRule()

    private val contractToulouse = BICContract(1, "Toulouse", 43.604652, 1.444209, "FR", 10, 6000.0, "https://")
    private val contractParis = BICContract(2, "Paris", 48.856614, 2.3522219, "FR", 10, 10000.0, "https://")
    private val contractColomiers = BICContract(3, "Colomiers", 43.610580, 1.334670, "FR", 10, 2000.0, "https://")
    private val contractBassoCambo = BICContract(4, "Basso Cambo", 43.570932, 1.392268, "FR", 10, 250.0, "https://")
    private val station1 = BICStation("Bréguet", 0.0, 0.0, 5, 15)
    private val station2 = BICStation("Ormeau", 0.0, 0.0, 9, 11)

    private val mockContractRepository = mock(BICContractRepository::class.java)

    private var viewModel: BICHomeViewModel? = null

    @Test
    fun testInit() {
        assertThat(viewModel, notNullValue())
        assertThat(viewModel!!.states, notNullValue())
        assertThat(viewModel!!.events, notNullValue())
    }

    @Test
    fun testGetAllContracts() {

        `when`(mockContractRepository.loadAllContracts()).thenReturn(Single.just(arrayListOf(contractToulouse, contractParis)))

        viewModel!!.getAllContracts()

        verify(mockContractRepository).loadAllContracts()

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateShowContracts::class.java))

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventContractList::class.java))
    }

    @Test
    fun testGetAllContractsWithError() {

        `when`(mockContractRepository.loadAllContracts()).thenReturn(Single.error(TimeoutException()))

        viewModel!!.getAllContracts()

        verify(mockContractRepository).loadAllContracts()

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateShowContracts::class.java))

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventFailure::class.java))
    }

    @Test
    fun testGetStationsForContract() {

        `when`(mockContractRepository.loadStationsBy(contractToulouse)).thenReturn(Single.just(arrayListOf(station1, station2)))

        viewModel!!.getStationsFor(contractToulouse)

        verify(mockContractRepository).loadStationsBy(contractToulouse)

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateShowStations::class.java))

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventStationList::class.java))

        val event = viewModel!!.events.value as EventStationList
        assertThat(event.stations.count(), `is`(equalTo(2)))
    }

    @Test
    fun testGetStationsForContractWithError() {

        `when`(mockContractRepository.loadStationsBy(contractToulouse)).thenReturn(Single.error(TimeoutException()))

        viewModel!!.getStationsFor(contractToulouse)

        verify(mockContractRepository).loadStationsBy(contractToulouse)

        assertThat(viewModel!!.states.value, notNullValue())
        assertThat(viewModel!!.states.value, instanceOf(StateShowStations::class.java))

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventFailure::class.java))
    }

    @Test
    fun testRefreshStationsForContract() {

        `when`(mockContractRepository.reloadStationsBy(contractToulouse)).thenReturn(Single.just(arrayListOf(station1, station2)))

        viewModel!!.refreshStationsFor(contractToulouse)

        verify(mockContractRepository).reloadStationsBy(contractToulouse)

        assertThat(viewModel!!.states.value, nullValue())

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventStationList::class.java))

        val event = viewModel!!.events.value as EventStationList
        assertThat(event.stations.count(), `is`(equalTo(2)))
    }

    @Test
    fun testRefreshStationsForContractWithError() {

        `when`(mockContractRepository.reloadStationsBy(contractToulouse)).thenReturn(Single.error(TimeoutException()))

        viewModel!!.refreshStationsFor(contractToulouse)

        verify(mockContractRepository).reloadStationsBy(contractToulouse)

        assertThat(viewModel!!.states.value, nullValue())

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventFailure::class.java))
    }

    @Test
    fun testDetermineCurrentContractWhenNoCurrent() {

        assertThat(viewModel!!.currentContract, nullValue())

        `when`(mockContractRepository.getContractBy(contractBassoCambo.bounds.center)).thenReturn(contractToulouse)

        viewModel!!.determineCurrentContract(contractBassoCambo.bounds)

        verify(mockContractRepository).getContractBy(contractBassoCambo.bounds.center)

        assertThat(viewModel!!.states.value, nullValue())

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventNewContract::class.java))

        val event = viewModel!!.events.value as EventNewContract
        assertThat(event.current, `is`(equalTo(contractToulouse)))
    }

    @Test
    fun testDetermineCurrentContractWhenSameThanCurrent() {

        viewModel!!.currentContract = contractToulouse
        assertThat(viewModel!!.currentContract, notNullValue())

        `when`(mockContractRepository.getContractBy(contractBassoCambo.bounds.center)).thenReturn(contractToulouse)

        viewModel!!.determineCurrentContract(contractBassoCambo.bounds)

        verify(mockContractRepository, times(0)).getContractBy(contractBassoCambo.bounds.center)

        assertThat(viewModel!!.states.value, nullValue())

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventSameContract::class.java))
    }

    @Test
    fun testDetermineCurrentContractWhenOutOfAnyContractAndNoCurrent() {

        assertThat(viewModel!!.currentContract, nullValue())

        `when`(mockContractRepository.getContractBy(contractColomiers.bounds.center)).thenReturn(null)

        viewModel!!.determineCurrentContract(contractColomiers.bounds)

        verify(mockContractRepository).getContractBy(contractColomiers.bounds.center)

        assertThat(viewModel!!.states.value, nullValue())

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventOutOfAnyContract::class.java))
    }

    @Test
    fun testDetermineCurrentContractWhenOutOfAnyContractAndCurrentOne() {

        viewModel!!.currentContract = contractToulouse
        assertThat(viewModel!!.currentContract, notNullValue())

        `when`(mockContractRepository.getContractBy(contractColomiers.bounds.center)).thenReturn(null)

        viewModel!!.determineCurrentContract(contractColomiers.bounds)

        verify(mockContractRepository).getContractBy(contractColomiers.bounds.center)

        assertThat(viewModel!!.states.value, nullValue())

        assertThat(viewModel!!.events.value, notNullValue())
        assertThat(viewModel!!.events.value, instanceOf(EventOutOfAnyContract::class.java))
    }

    @Before
    fun setUp() {
        viewModel = BICHomeViewModel(mockContractRepository)
    }

    @After
    fun tearDown() {
        viewModel = null
    }
}
