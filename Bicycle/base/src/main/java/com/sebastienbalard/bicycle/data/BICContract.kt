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

package com.sebastienbalard.bicycle.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.annotations.SerializedName
import com.google.maps.android.SphericalUtil
import java.lang.Math.sqrt

@Entity(tableName = "bic_contracts")
data class BICContract(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "pk_contract_id")
        @NonNull
        val id: Long,
        val name: String,
        @SerializedName("lat") val latitude: Double,
        @SerializedName("lng") val longitude: Double,
        val provider: Provider,
        val radius: Double,
        val url: String) {

    val center: LatLng
        get() = LatLng(latitude, longitude)

    val bounds: LatLngBounds
        get() {
            val distanceFromCenterToCorner = radius * sqrt(2.0)
            val southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
            val northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
            return LatLngBounds(southwestCorner, northeastCorner)
        }

    enum class Provider(val tag: String) {
        @SerializedName("CityBikes") CityBikes("CityBikes");

        companion object {
            fun from(tag: String): Provider = Provider.values().first { it.tag == tag }
        }
    }
}