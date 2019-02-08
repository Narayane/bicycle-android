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

package com.sebastienbalard.bicycle.views.home

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.sebastienbalard.bicycle.models.BICStation

open class BICStationAnnotation(val station: BICStation) : ClusterItem {

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return station.location
    }

    open class Renderer(context: Context, map: GoogleMap, clusterManager: ClusterManager<BICStationAnnotation>) : DefaultClusterRenderer<BICStationAnnotation>(context, map, clusterManager) {

        override fun onBeforeClusterRendered(cluster: Cluster<BICStationAnnotation>?, markerOptions: MarkerOptions?) {
            markerOptions?.snippet(null)
            super.onBeforeClusterRendered(cluster, markerOptions)
        }

        override fun onBeforeClusterItemRendered(item: BICStationAnnotation?, markerOptions: MarkerOptions?) {
            item?.station?.let { station ->
                markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(station.icon)).snippet("type=station")
            }
            super.onBeforeClusterItemRendered(item, markerOptions)
        }

        override fun onClusterItemRendered(clusterItem: BICStationAnnotation?, marker: Marker?) {
            clusterItem?.let { item ->
                marker?.tag = item.station
            }
            super.onClusterItemRendered(clusterItem, marker)
        }
    }
}