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

package com.sebastienbalard.bicycle.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.sebastienbalard.bicycle.BICApplication
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.extensions.drawOn
import com.sebastienbalard.bicycle.extensions.getBitmap
import kotlin.properties.Delegates

data class BICStation(val name: String,
                      private val latitude: Double,
                      private val longitude: Double,
                      @SerializedName("free_bikes") val availableBikesCount: Int,
                      @SerializedName("empty_slots") val freeStandsCount: Int) {

    companion object {
        var size: Int by Delegates.notNull()
        var textSize: Int by Delegates.notNull()
        lateinit var imageStation: Bitmap
        var textColor: Int by Delegates.notNull()

        fun initConstants(context: Context) {
            size = context.resources.getDimensionPixelSize(R.dimen.bic_size_annotation)
            textSize = context.resources.getDimensionPixelSize(R.dimen.bic_size_font_body)
            imageStation = context.getBitmap(R.drawable.bic_img_station, size, size)
            textColor = ContextCompat.getColor(context, R.color.bic_color_white)
        }
    }

    val location: LatLng
        get() = LatLng(latitude, longitude)

    val icon: Bitmap
        get() {
            var bitmap = imageStation.drawOn(availableBikesCount.toString(), textColor, textSize.toFloat(), 3.75f)
            bitmap = bitmap.drawOn(freeStandsCount.toString(), textColor, textSize.toFloat(), 1.65f)
            return bitmap
        }
}