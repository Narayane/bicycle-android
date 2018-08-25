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

package com.sebastienbalard.bicycle.extensions

import com.google.android.gms.maps.model.LatLng

fun String.fromPolyline(): ArrayList<LatLng> {

    val listPoints = ArrayList<LatLng>()
    var index = 0
    var latitude = 0
    var longitude = 0

    while (index < this.length) {

        var bit: Int
        var shift = 0
        var result = 0

        do {
            bit = this[index++].toInt() - 63
            result = result or (bit and 0x1f shl shift)
            shift += 5
        } while (bit >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        latitude += dlat

        shift = 0
        result = 0

        do {
            bit = this[index++].toInt() - 63
            result = result or (bit and 0x1f shl shift)
            shift += 5
        } while (bit >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        longitude += dlng

        val point = LatLng(latitude.toDouble() / 1E5, longitude.toDouble() / 1E5)
        listPoints.add(point)
    }

    return listPoints
}