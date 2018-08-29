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

package com.sebastienbalard.bicycle

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.sebastienbalard.bicycle.extensions.*
import kotlinx.android.synthetic.main.sb_widget_appbar.*
import kotlinx.android.synthetic.main.sb_widget_mapview.*
import org.koin.android.viewmodel.ext.android.viewModel

abstract class SBMapActivity : SBActivity() {

    companion object : SBLog()

    protected val viewModelMap: SBMapViewModel by viewModel()

    protected var googleMap: GoogleMap? = null
    protected var selectedMarker: Marker? = null

    //region Lifecycle methods

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        requestLocationPermissionsIfNeeded(NOTIFICATION_REQUEST_PERMISSION_LOCATION,
                onGranted = {
                    viewModelMap.userLocation.observe(this, Observer<Location> { location ->
                    refreshMap()
                    onUserLocationChanged(location)
                })
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_REQUEST_PERMISSION_LOCATION ->
                processPermissionsResults(permissions, grantResults,
                        onGranted = {
                            viewModelMap.userLocation.observe(this, Observer<Location> { location ->
                                refreshMap()
                                onUserLocationChanged(location)
                            })
                        },
                        onDenied = {
                            refreshMap()
                            showWarningForLocationPermission()
                        })
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    //endregion

    //region Protected methods

    protected abstract fun onMapInitialized()

    protected abstract fun onMapRefreshed(hasLocationPermissions: Boolean)

    protected abstract fun onUserLocationChanged(location: Location?)

    protected abstract fun onMarkerClicked(marker: Marker)

    protected abstract fun onMapClicked()

    protected abstract fun onCameraIdle()

    protected fun initMap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        mapView.alignZoomControls(this, RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.ALIGN_PARENT_RIGHT)
        mapView.getMapAsync { map ->

            googleMap = map
            if (googleMap != null) {
                onMapInitialized()
                googleMap!!.setOnMapClickListener {
                    v("onMapClick")
                    onMapClicked()
                }
                googleMap!!.setOnMarkerClickListener { marker ->
                    if (selectedMarker != null && selectedMarker!! == marker) {
                        true // current selected marker clicked
                    } else {
                        v("onMarkerClick")
                        onMarkerClicked(marker)
                        false
                    }
                }
                googleMap!!.setOnCameraIdleListener {
                    v("onCameraIdle")
                    onCameraIdle()
                }
                googleMap!!.uiSettings.isCompassEnabled = true
                googleMap!!.uiSettings.isZoomControlsEnabled = true
                googleMap!!.uiSettings.isMapToolbarEnabled = false

            } else {
                Snackbar.make(toolbar, R.string.bic_messages_error_no_play_services_installed, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    //endregion

    //region Private methods

    private fun showWarningForLocationPermission() {
        val snackbar = Snackbar.make(toolbar, R.string.bic_messages_warning_request_location_permissions, Snackbar.LENGTH_LONG)
        snackbar.setAction(R.string.bic_actions_allow) {
            startActivityForResult(getIntentForApplicationSettings(), NOTIFICATION_REQUEST_PERMISSION_LOCATION)
        }.setActionTextColor(ContextCompat.getColor(this, R.color.bic_color_white))
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.bic_color_red))
        val textView = snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
        textView.setTextColor(ContextCompat.getColor(this, R.color.bic_color_white))
        snackbar.show()
    }

    @SuppressLint("MissingPermission")
    private fun refreshMap() {
        val hasLocationPermissions = hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        googleMap?.let {
            it.isMyLocationEnabled = hasLocationPermissions
            it.uiSettings.isMyLocationButtonEnabled = hasLocationPermissions
        }
        onMapRefreshed(hasLocationPermissions)
    }

    //endregion
}
