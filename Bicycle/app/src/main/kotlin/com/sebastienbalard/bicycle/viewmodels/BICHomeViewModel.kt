/**
 * Copyright © 2017 Bicycle (Sébastien BALARD)
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

import com.google.android.gms.maps.model.LatLngBounds
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.extensions.intersect
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.android.schedulers.AndroidSchedulers

object StateShowContracts : State()
object StateShowStations : State()

object EventOutOfAnyContract : Event()
data class EventNewContract(val current: BICContract) : Event()
object EventSameContract : Event()
data class EventContractList(val contracts: List<BICContract>) : Event()
data class EventStationList(val stations: List<BICStation>) : Event()

class BICHomeViewModel(private val contractRepository: BICContractRepository) : SBViewModel() {

    companion object : SBLog()

    var currentContract: BICContract? = null

    fun getAllContracts() {
        _states.value = StateShowContracts
        launch {
            contractRepository.loadAllContracts()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ contracts ->
                        _events.value = EventContractList(contracts)
                    }, { error ->
                        _events.value = EventFailure(error)
                    })
        }
    }

    fun getStationsFor(contract: BICContract) {
        _states.value = StateShowStations
        launch {
            contractRepository.loadStationsBy(contract)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ stations ->
                        _events.value = EventStationList(stations)
                    }, { error ->
                        _events.value = EventFailure(error)
                    })
        }
    }

    fun refreshStationsFor(contract: BICContract) {
        launch {
            contractRepository.reloadStationsBy(contract)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ stations ->
                        _events.value = EventStationList(stations)
                    }, { error ->
                        _events.value = EventFailure(error)
                    })
        }
    }

    fun determineCurrentContract(from: LatLngBounds) {

        var invalidateCurrentContract = false
        var hasChanged = false
        var current: BICContract? = null

        currentContract?.let {
            if (!it.bounds.intersect(from)) {
                invalidateCurrentContract = true
            }
        }

        if (currentContract == null || invalidateCurrentContract) {
            current = contractRepository.getContractBy(from.center)
            hasChanged = current != null
        }

        if (current != null && hasChanged)  {
            currentContract = current
            _events.value = EventNewContract(current)
        } else if (currentContract != null && !invalidateCurrentContract) {
            _events.value = EventSameContract
        } else {
            currentContract = null
            _events.value = EventOutOfAnyContract
        }
    }
}