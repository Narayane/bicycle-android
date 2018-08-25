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

package com.sebastienbalard.bicycle.io

import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.SBLog
import com.sebastienbalard.bicycle.models.BICStation
import io.reactivex.Single

open class CityBikesDataSource(private val cityBikesApi: CityBikesApi) {

    companion object : SBLog()

    open fun getStationsBy(contract: BICContract): Single<List<BICStation>> {
        val contractName = contract.url.substring(contract.url.lastIndexOf('/') + 1)
        d("contract endpoint: $contractName (${contract.name})")
        return cityBikesApi.getStations(contractName).map { response -> response.network.stations }
    }
}