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
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.sebastienbalard.bicycle.data.BICContract

open class BICContractAnnotation(val contract: BICContract) : ClusterItem {

    override fun getPosition(): LatLng {
        return contract.center
    }

    open class Renderer(context: Context, map: GoogleMap, clusterManager: ClusterManager<BICContractAnnotation>) : DefaultClusterRenderer<BICContractAnnotation>(context, map, clusterManager) {

        override fun onBeforeClusterRendered(cluster: Cluster<BICContractAnnotation>?, markerOptions: MarkerOptions?) {
            markerOptions?.snippet(null)
            super.onBeforeClusterRendered(cluster, markerOptions)
        }

        override fun onBeforeClusterItemRendered(item: BICContractAnnotation?, markerOptions: MarkerOptions?) {
            item?.contract?.let { contract ->
                markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(contract.icon))?.snippet("type=contract")
            }
            super.onBeforeClusterItemRendered(item, markerOptions)
        }

        override fun onClusterItemRendered(clusterItem: BICContractAnnotation?, marker: Marker?) {
            clusterItem?.let { item ->
                marker?.tag = item.contract
            }
            super.onClusterItemRendered(clusterItem, marker)
        }
    }
}
