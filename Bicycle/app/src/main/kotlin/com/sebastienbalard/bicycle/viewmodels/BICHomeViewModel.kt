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
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.android.schedulers.AndroidSchedulers

class BICHomeViewModel(private val contractRepository: BICContractRepository) : SBViewModel() {

    companion object : SBLog()

    var currentContract: BICContract? = null

    fun getAllContracts() {
        _states.value = StateLoading
        launch {
            contractRepository.getAllContracts()
                    .subscribe(
                            { contracts -> _events.value = EventContractList(contracts) },
                            { error -> _events.value = EventFailure(error) })
        }
    }

    fun loadContractStations(contract: BICContract) {
        _states.value = StateLoading
        launch {
            contractRepository.getStationsFor(contract)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { stations -> _events.value = EventStationList(stations) },
                            { error -> _events.value = EventFailure(error) })
        }
    }

    fun refreshContractStations(contract: BICContract) {
        _states.postValue(StateLoading)
        launch {
            contractRepository.refreshStationsFor(contract)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { stations -> _events.postValue(EventStationList(stations)) },
                            { error -> _events.postValue(EventFailure(error)) })
        }
    }

    fun determineCurrentContract(from: LatLngBounds, with: BICContract? = null) {

        var invalidateCurrentContract = false
        var hasChanged = false
        var current: BICContract? = null

        with?.let {
            if (!it.bounds.intersect(from)) {
                invalidateCurrentContract = true
                hasChanged = true
            }
        }

        if (with == null || invalidateCurrentContract) {
            current = contractRepository.getContractFor(from.center)
            hasChanged = hasChanged || current != null
        }

        when {
            current != null ->  {
                currentContract = current
                _states.value = StateContract(current, hasChanged)
            }
            with != null -> {
                currentContract = with
                _states.value = StateContract(with, false)
            }
            else ->  {
                currentContract = null
                _states.value = StateOutOfContract
            }
        }
    }
}