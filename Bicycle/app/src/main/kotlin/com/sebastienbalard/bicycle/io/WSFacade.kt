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

import com.google.android.gms.maps.model.LatLng
import com.sebastienbalard.bicycle.BICApplication
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.io.dtos.BICContractsDataResponseDto
import com.sebastienbalard.bicycle.io.dtos.GMDirectionsResponseDto
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.models.BICStation
import io.reactivex.Single

class WSFacade {

    companion object: SBLog() {

        fun getStationsByContract(contract: BICContract): Single<List<BICStation>> {
            val contractName = contract.url.substring(contract.url.lastIndexOf('/') + 1)
            d("contract endpoint: $contractName (${contract.name})")
            return CityBikesApi.instance.getStations(contractName).map { response -> response.network.stations }
        }

        fun getDirections(mode: String, from: LatLng, to: LatLng, vararg steps: LatLng): Single<GMDirectionsResponseDto> {
            return if (steps.isEmpty()) {
                GoogleMapsApi.instance.getDirections("${from.latitude},${from.longitude}", "${to.latitude},${to.longitude}",
                        mode, true, BICApplication.context.getString(R.string.key_google_maps_directions))
            } else {
                GoogleMapsApi.instance.getDirectionsViaWaypoints("${from.latitude},${from.longitude}", "${to.latitude},${to.longitude}",
                        steps.joinToString(separator = "|", transform = { latLng -> "${latLng.latitude},${latLng.longitude}" }),
                        mode, true, BICApplication.context.getString(R.string.key_google_maps_directions))
            }
        }
    }
}