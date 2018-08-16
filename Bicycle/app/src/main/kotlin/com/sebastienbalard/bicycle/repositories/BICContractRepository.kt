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

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.data.BICContractDao
import com.sebastienbalard.bicycle.extensions.distanceTo
import com.sebastienbalard.bicycle.extensions.formatDate
import com.sebastienbalard.bicycle.io.BicycleApi
import com.sebastienbalard.bicycle.io.WSFacade
import com.sebastienbalard.bicycle.misc.BICSharedPreferences
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICStation
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class BICContractRepository(private val bicycleApi: BicycleApi, private val contractDao: BICContractDao) {

    companion object : SBLog()

    private var allContracts = ArrayList<BICContract>()
    private var cacheStations = HashMap<String, List<BICStation>>()

    fun loadAllContracts(): Observable<Event> {

        var timeToCheck = true
        val now = DateTime.now()

        BICSharedPreferences.contractsLastCheckDate?.let {
            v("last check: ${it.formatDate(BICApplication.context)}")
            timeToCheck = Days.daysBetween(it.toLocalDate(), now.toLocalDate()).days > BuildConfig
                    .DAYS_BETWEEN_CONTRACTS_CHECK
        }

        return if (timeToCheck) {
            d("get contracts from remote")
            Observable.create<Event> { observer ->
                observer.onNext(EventMessage(BICApplication.context.getString(R.string.bic_messages_info_check_contracts_data_version)))
                bicycleApi.getContractsData("media")
                        .observeOn(Schedulers.computation())
                        .subscribe(
                                { response ->
                                    if (response.version > BICSharedPreferences.contractsVersion) {
                                        observer.onNext(EventMessage(BICApplication.context.getString(R.string.bic_messages_info_update_contracts)))
                                        val existing = contractDao.findAll()
                                        d("delete ${existing.size} contracts")
                                        contractDao.deleteAll(existing as ArrayList<BICContract>)
                                        contractDao.insertAll(response.values)
                                        BICSharedPreferences.contractsVersion = response.version
                                    } else {
                                        d("contracts are up-to-date")
                                    }
                                    BICSharedPreferences.contractsLastCheckDate = now
                                    observer.onNext(EventMessage(BICApplication.context.getString(R.string.bic_messages_info_contracts_loaded, contractDao.getAllCount())))
                                    Thread.sleep(3000L)
                                    observer.onComplete()
                                },
                                { error -> observer.onError(error) })

            }.subscribeOn(Schedulers.newThread())
        } else {
            d("contracts are up-to-date")
            BICSharedPreferences.contractsLastCheckDate = now
            Observable.just(1)
                    .subscribeOn(Schedulers.computation())
                    .map { _ -> EventMessage(BICApplication.context.getString(R.string.bic_messages_info_contracts_loaded, contractDao.getAllCount())) }
                    /*.concatMap { Observable.empty<Event>().delay(3L, TimeUnit.SECONDS) }
                    .subscribeOn(Schedulers.newThread())*/
        }
    }

    fun getAllContracts(): Single<List<BICContract>> {
        d("get contracts from local")
        return Single.fromObservable(Observable.fromArray(contractDao.findAll()))
    }

    fun getContractFor(location: Location): BICContract? {
        return getContractFor(LatLng(location.latitude, location.longitude))
    }

    fun getContractFor(latLng: LatLng): BICContract? {
        val filteredList = allContracts.filter { contract -> contract.bounds.contains(latLng) }
        var rightContract: BICContract? = null
        if (filteredList.isNotEmpty()) {
            rightContract = filteredList.first()
            if (filteredList.size > 1) {
                var minDistance: Float? = null
                var distanceFromCenter: Float?
                for (filtered in filteredList) {
                    distanceFromCenter = latLng.distanceTo(filtered.center)
                    if (minDistance == null) {
                        minDistance = distanceFromCenter
                        rightContract = filtered
                    } else if (minDistance > distanceFromCenter) {
                        minDistance = distanceFromCenter
                        rightContract = filtered
                    }
                }
            }
        }
        return rightContract
    }

    fun getStationsFor(contract: BICContract): Single<List<BICStation>> {
        return if (cacheStations.containsKey(contract.name)) {
            //Observable.fromIterable(cacheStations.getValue(contract.name))
            Single.fromObservable(Observable.fromArray(cacheStations.getValue(contract.name)))
        } else {
            refreshStationsFor(contract)
        }
    }

    fun refreshStationsFor(contract: BICContract): Single<List<BICStation>> {
        return WSFacade.getStationsByContract(contract)
                .doOnSuccess { stations -> cacheStations[contract.name] = stations }
                .doOnError { throwable -> e("fail to get contract stations", throwable) }
    }

    /*private fun loadContracts() {
        try {
            val inputStream = BICApplication.context.assets.open("contracts.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charset.forName("UTF-8"))

            allContracts = Gson().fromJson(json, object : TypeToken<ArrayList<BICContract>>() {}.type)
            d("${allContracts.size} contracts loaded")
        } catch (exception: IOException) {
            e("fail to load contracts from assets", exception)
        }
    }*/
}
