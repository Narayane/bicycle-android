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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLngBounds
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.extensions.intersect
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICContract
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.android.schedulers.AndroidSchedulers

class BICHomeViewModel(private val contractRepository: BICContractRepository) : SBViewModel() {

    companion object : SBLog()

    private val _states = MutableLiveData<State>()
    val states: LiveData<State>
        get() = _states

    private val _events = MutableLiveData<Event>()
    val events: LiveData<Event>
        get() = _events

    fun getAllContracts(): List<BICContract> {
        return contractRepository.allContracts
    }

    fun loadContractStations(contract: BICContract) {
        _states.value = LoadingState
        launch {
            contractRepository.getStationsFor(contract)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { stations -> _events.value = StationListEvent(stations) },
                            { error -> _events.value = FailureEvent(error) })
        }
    }

    fun refreshContractStations(contract: BICContract) {
        _states.value = LoadingState
        launch {
            contractRepository.refreshStationsFor(contract)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { stations -> _events.value = StationListEvent(stations) },
                            { error -> _events.value = FailureEvent(error) })
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
            current != null -> _states.value = ContractState(current, hasChanged)
            with != null -> _states.value = ContractState(with, false)
            else -> _states.value = OutOfContractState
        }
    }
}