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
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.data.BICContractDao
import com.sebastienbalard.bicycle.extensions.distanceTo
import com.sebastienbalard.bicycle.io.BicycleDataSource
import com.sebastienbalard.bicycle.io.CityBikesDataSource
import com.sebastienbalard.bicycle.SBLog
import com.sebastienbalard.bicycle.models.BICStation
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.async
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList

open class BICContractRepository(private val bicycleDataSource: BicycleDataSource,
                                 private val cityBikesDataSource: CityBikesDataSource,
                                 private val contractDao: BICContractDao,
                                 private val preferenceRepository: BICPreferenceRepository) {

    companion object : SBLog()

    private var allContracts = ArrayList<BICContract>()
    private var cacheStations = HashMap<String, List<BICStation>>()

    init {
        async {
            allContracts.addAll(contractDao.findAll())
        }
    }

    open fun updateContracts(): Single<Int> {
        return Single.create<Int> { observer ->
            bicycleDataSource.getContracts()
                    .observeOn(Schedulers.computation())
                    .subscribe({ response ->
                        if (response.version > preferenceRepository.contractsVersion) {
                            val existing = contractDao.findAll()
                            d("delete ${existing.size} contracts")
                            contractDao.deleteAll(existing as ArrayList<BICContract>)
                            contractDao.insertAll(response.values)
                            preferenceRepository.contractsVersion = response.version
                        } else {
                            d("contracts are up-to-date")
                        }
                        preferenceRepository.contractsLastCheckDate = DateTime.now()
                        val count = contractDao.getAllCount()
                        d("find $count contracts")
                        observer.onSuccess(count)
                    }, { error ->
                        observer.onError(error)
                    })
        }.subscribeOn(Schedulers.newThread())
    }

    open fun getContractCount(): Single<Int> {
        return Single.create<Int> { observer ->
            observer.onSuccess(contractDao.getAllCount())
        }.subscribeOn(Schedulers.newThread())
    }

    open fun loadAllContracts(): Single<List<BICContract>> {
        d("get contracts from local")
        return Single.create<List<BICContract>> { observer ->
            observer.onSuccess(contractDao.findAll())
        }.subscribeOn(Schedulers.newThread())
    }

    fun getContractBy(location: Location): BICContract? {
        return getContractBy(LatLng(location.latitude, location.longitude))
    }

    open fun getContractBy(latLng: LatLng): BICContract? {
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

    open fun loadStationsBy(contract: BICContract): Single<List<BICStation>> {
        return if (cacheStations.containsKey(contract.name)) {
            Single.fromObservable(Observable.fromArray(cacheStations.getValue(contract.name))).subscribeOn(Schedulers.newThread())
        } else {
            reloadStationsBy(contract)
        }
    }

    open fun reloadStationsBy(contract: BICContract): Single<List<BICStation>> {
        return cityBikesDataSource.getStationsByContract(contract)
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
