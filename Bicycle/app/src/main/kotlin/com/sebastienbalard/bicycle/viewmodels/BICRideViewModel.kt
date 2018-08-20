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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sebastienbalard.bicycle.extensions.distanceTo
import com.sebastienbalard.bicycle.extensions.flatMapToObservable
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICPlace
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class BICRideViewModel(private val contractRepository: BICContractRepository) : ViewModel() {

    companion object : SBLog()

    private val disposables: CompositeDisposable = CompositeDisposable()

    lateinit var departure: BICPlace
    lateinit var arrival: BICPlace
    var bikesCount: Int = 1
    var freeSlotsCount: Int = 1
    var departureNearestStations = MutableLiveData<List<BICStation>>()
    var arrivalNearestStations =  MutableLiveData<List<BICStation>>()
    var hasRoute = MutableLiveData<Boolean>()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun determineNearestStations() {

        departure.contract = contractRepository.getContractBy(departure.location)
        departure.contract?.let {
            v("departure place contract is ${it.name}")
        }
        arrival.contract = contractRepository.getContractBy(arrival.location)
        arrival.contract?.let {
            v("arrival place contract is ${it.name}")
        }

        var radius = departure.location.distanceTo(arrival.location)
        if (radius > 500f) {
            radius = 500f
        }

        val observableStations = contractRepository.reloadStationsBy(departure.contract!!)
                .flatMapToObservable()

        disposables.add(observableStations
                .filter { station -> station.location.distanceTo(departure.location) <= radius && station.availableBikesCount >= bikesCount }
                .toSortedList { station1, station2 ->
                    station1.location.distanceTo(departure.location).compareTo(station2.location.distanceTo(departure.location))
                }
                .doOnSuccess { sortedStations -> v("take 3 nearest from departure on ${sortedStations.size}") }
                .doOnError { error -> e("fail to find 3 nearest from departure", error) }
                .flatMapToObservable().take(3).toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    nearestStations -> run {
                        departureNearestStations.value = nearestStations
                        hasRoute.value = departureNearestStations.value != null && arrivalNearestStations.value != null
                    }
                }, {
                    _ -> departureNearestStations.value = null ; hasRoute.value = false
                })
        )
        disposables.add(observableStations
                .filter { station -> station.location.distanceTo(arrival.location) <= radius && station.freeStandsCount >= freeSlotsCount }
                .toSortedList { station1, station2 ->
                    station1.location.distanceTo(arrival.location).compareTo(station2.location.distanceTo(arrival.location))
                }
                .doOnSuccess { sortedStations -> v("take 3 nearest from arrival on ${sortedStations.size}") }
                .doOnError { error -> e("fail to find 3 nearest from departure", error) }
                .flatMapToObservable().take(3).toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    nearestStations -> run {
                        arrivalNearestStations.value = nearestStations
                        hasRoute.value = departureNearestStations.value != null && arrivalNearestStations.value != null
                    }
                }, {
                    _ -> arrivalNearestStations.value = null ; hasRoute.value = false
                })
        )
    }

}
