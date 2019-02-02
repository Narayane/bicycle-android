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
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.NonNull
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.annotations.SerializedName
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.computeNorthEastBoundsCorner
import com.sebastienbalard.bicycle.extensions.computeSouthWestBoundsCorner
import com.sebastienbalard.bicycle.extensions.getBitmap
import java.lang.Math.sqrt
import java.util.*
import kotlin.properties.Delegates

@Entity(tableName = "bic_contracts")
data class BICContract(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "pk_contract_id")
        @NonNull
        val id: Long,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        @ColumnInfo(name = "country_code")
        @SerializedName("country") val countryCode: String,
        @ColumnInfo(name = "station_count")
        @SerializedName("station_count") val stationCount: Int,
        val radius: Double,
        val url: String) {

    val center: LatLng
        get() = LatLng(latitude, longitude)

    val bounds: LatLngBounds
        get() {
            val distanceFromCenterToCorner = radius * sqrt(2.0)
            val southwestCorner = center.computeSouthWestBoundsCorner(distanceFromCenterToCorner)
            val northeastCorner = center.computeNorthEastBoundsCorner(distanceFromCenterToCorner)
            return LatLngBounds(southwestCorner, northeastCorner)
        }

    val icon: Bitmap
        get() = imageContract

    val iconSelected: Bitmap
        get() = imageContractSelected

    val countryName: String
        get() {
            val locale = Locale("", countryCode)
            return locale.displayCountry
        }

    companion object {
        var size: Int by Delegates.notNull()
        lateinit var imageContract: Bitmap
        lateinit var imageContractSelected: Bitmap

        fun initConstants(context: Context) {
            size = context.resources.getDimensionPixelSize(R.dimen.bic_size_annotation)
            imageContract = context.getBitmap(R.drawable.bic_img_contract, size, size)
            imageContractSelected = context.getBitmap(R.drawable.bic_img_contract_selected, size, size)
        }
    }
}