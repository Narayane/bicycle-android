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

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.viewmodels.*
import com.sebastienbalard.bicycle.views.BICAboutActivity
import com.sebastienbalard.bicycle.views.settings.BICSettingsActivity
import kotlinx.android.synthetic.main.bic_activity_home.*
import kotlinx.android.synthetic.main.sb_widget_appbar.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.concurrent.timerTask

class BICHomeActivity : SBMapActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICHomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private val viewModelHome: BICHomeViewModel by viewModel()

    private var clusterContracts: ClusterManager<BICContractAnnotation>? = null
    private var clusterStations: ClusterManager<BICStationAnnotation>? = null
    private var timer: Timer? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>

    private var previousZoomLevel: Int? = null

    //region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_home)
        v("onCreate")
        initToolbar()
        initMap(savedInstanceState)
        initObservers()
        initLayout()
    }

    override fun onStart() {
        super.onStart()
        v("onStart")
        startTimer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        v("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.bic_menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.bic_menu_home_item_settings -> {
                i("click on menu item: settings")
                startActivity(BICSettingsActivity.getIntent(this))
                true
            }
            R.id.bic_menu_home_item_about -> {
                i("click on menu item: about")
                startActivity(BICAboutActivity.getIntent(this))
                //Toast.makeText(this, R.string.bic_messages_available_soon, Toast.LENGTH_LONG).showAsError(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        v("onStop")
        stopTimer()
    }

    //endregion

    @Override
    override fun initToolbar() {
        super.initToolbar()
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        } catch (exception: NullPointerException) {
            // do nothing
        }
    }

    //region Map events

    override fun onMapInitialized() {
        clusterContracts = ClusterManager(this, googleMap!!)
        clusterContracts?.renderer = BICContractAnnotation.Renderer(this, googleMap!!, clusterContracts!!)
        clusterStations = ClusterManager(this, googleMap!!)
        clusterStations?.renderer = BICStationAnnotation.Renderer(this, googleMap!!, clusterStations!!)
    }

    override fun onMapRefreshed(hasLocationPermissions: Boolean) {

    }

    override fun onUserLocationChanged(location: Location?) {
        location?.let {
            v("move camera to new user location")
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
            googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f), 1000, null)
        }
    }

    override fun onMarkerClicked(marker: Marker) {
        showBottomSheet(marker)
    }

    override fun onMapClicked() {
        hideBottomSheet()
    }

    override fun onCameraIdle() {
        refreshMarkers()
    }

    //endregion

    //region Private methods

    private fun showBottomSheet(marker: Marker) {
        v("showBottomSheet")
        deselectMarker(selectedMarker)
        if (marker.snippet == null) {
            viewModelHome.states.value?.apply {
                when (this) {
                    is StateShowContracts -> fabContractZoom.visibility = View.INVISIBLE
                    else -> fabContractZoom.visibility = View.GONE
                }
            }
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            selectedMarker = null
        } else {
            refreshBottomSheetLayout(marker)
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            selectMarker(marker)
            viewModelHome.states.value?.apply {
                when (this) {
                    is StateShowContracts -> fabContractZoom.visibility = View.VISIBLE
                    else -> fabContractZoom.visibility = View.GONE
                }
            }
        }
    }

    private fun hideBottomSheet() {
        v("hideBottomSheet")
        viewModelHome.states.value?.apply {
            when (this) {
                is StateShowContracts -> fabContractZoom.visibility = View.INVISIBLE
                else -> fabContractZoom.visibility = View.GONE
            }
        }
        deselectMarker(selectedMarker)
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        selectedMarker = null
    }

    private fun refreshBottomSheetLayout(marker: Marker) {
        marker.snippet?.apply {
            if (startsWith("type=")) {
                if (endsWith("station")) {
                    (marker.tag as? BICStation)?.apply {
                        textViewBottomSheetTitle.text = displayName
                        textViewBottomSheetSubtitle.text = viewModelHome.currentContract?.name ?: "-"
                        textViewBottomSheetAvailableBikesCount.text = resources.getQuantityString(R.plurals.bic_plurals_available_bikes, availableBikesCount, availableBikesCount)
                        textViewBottomSheetFreeStandsCount.text = resources.getQuantityString(R.plurals.bic_plurals_free_stands, freeStandsCount, freeStandsCount)
                    }
                } else if (endsWith("contract")) {
                    (marker.tag as? BICContract)?.apply {
                        textViewBottomSheetTitle.text = name
                        textViewBottomSheetSubtitle.text = countryName
                        textViewBottomSheetAvailableBikesCount.text = ""
                        textViewBottomSheetFreeStandsCount.text = resources.getQuantityString(R.plurals.bic_plurals_station_count, stationCount, stationCount)
                    }
                }
            }
        }
    }

    private fun selectMarker(marker: Marker?) {
        marker?.snippet?.apply {
            if (startsWith("type=")) {
                if (endsWith("station")) {
                    (marker.tag as? BICStation)?.apply {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconSelected))
                    }
                } else if (endsWith("contract")) {
                    (marker.tag as? BICContract)?.apply {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconSelected))
                    }
                }
                selectedMarker = marker
            }
        }
    }

    private fun deselectMarker(marker: Marker?) {
        marker?.snippet?.apply {
            if (startsWith("type=")) {
                if (endsWith("station")) {
                    (marker.tag as? BICStation)?.apply {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
                    }
                } else if (endsWith("contract")) {
                    (marker.tag as? BICContract)?.apply {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
                    }
                }
            }
        }
    }

    private fun showErrorForCurrentContractStation() {
        val snackbar = Snackbar.make(toolbar, R.string.bic_messages_warning_get_current_contract_stations, Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.bic_color_red))
        val textView = snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
        textView.setTextColor(ContextCompat.getColor(this, R.color.bic_color_white))
        snackbar.show()
    }

    private fun startTimer() {
        val zoomLevel = googleMap?.cameraPosition?.zoom?.toInt()
        if (timer == null && viewModelHome.currentContract != null && zoomLevel != null && zoomLevel >= 10) {
            val delay = BuildConfig.TIME_BEFORE_REFRESH_STATIONS_DATA_IN_SECONDS * 1000
            d("start timer")
            timer = Timer()
            timer!!.scheduleAtFixedRate(timerTask {
                d("timer fired")
                viewModelHome.currentContract?.let {
                    d("refresh contract stations: ${it.name}")
                    viewModelHome.refreshStationsFor(it)
                }
            }, delay, delay)
        }
    }

    private fun stopTimer() {
        timer?.apply {
            d("stop timer")
            cancel()
            timer = null
        }
    }

    private fun refreshMarkers() {
        zoomLevel?.let { zoomLevel ->
            d("current zoom level: $zoomLevel")
            previousZoomLevel?.let { previous ->
                if (zoomLevel != previous) {
                    hideBottomSheet()
                }
            }
            previousZoomLevel = zoomLevel
            if (zoomLevel >= 10) {
                if (!haveStationAnnotations()) {
                    clusterContracts?.clearItems()
                    clusterContracts?.cluster()
                }
                viewModelHome.determineCurrentContract(visibleRegion!!.latLngBounds)
            } else {
                stopTimer()
                if (!haveContractAnnotations()) {
                    clusterStations?.clearItems()
                    clusterStations?.cluster()
                    viewModelHome.getAllContracts()
                } else {
                    clusterContracts?.cluster()
                }
            }
        }
    }

    private fun haveContractAnnotations(): Boolean {
        val hasMarkers = clusterContracts?.markerCollection?.markers?.isNotEmpty()?.or(false)!!
        val hasClusterMarkers = clusterContracts?.clusterMarkerCollection?.markers?.isNotEmpty()?.or(false)!!
        return hasMarkers || hasClusterMarkers
    }

    private fun haveStationAnnotations(): Boolean {
        val hasMarkers = clusterStations?.markerCollection?.markers?.isNotEmpty()?.or(false)!!
        val hasClusterMarkers = clusterStations?.clusterMarkerCollection?.markers?.isNotEmpty()?.or(false)!!
        return hasMarkers || hasClusterMarkers
    }

    private fun initLayout() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        fabContractZoom.setOnClickListener {
            selectedMarker?.tag?.apply {
                when (this) {
                    is BICContract -> {
                        hideBottomSheet()
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 12f))
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun initObservers() {
        viewModelHome.states.observe(this, Observer { state ->
            state?.apply {
                when (this) {
                    is StateShowContracts -> fabContractZoom.visibility = View.INVISIBLE
                    is StateShowStations -> fabContractZoom.visibility = View.GONE
                    else -> w("unexpected state")
                }
            }
        })
        viewModelHome.events.observe(this, Observer { event ->
            event?.let {
                when (it) {
                    is EventContractList -> {
                        clusterContracts?.clearItems()
                        hideBottomSheet()
                        it.contracts.map { contract ->
                            clusterContracts?.addItem(BICContractAnnotation(contract))
                        }
                        clusterContracts?.cluster()
                    }
                    is EventOutOfAnyContract -> {
                        d("current bounds is out of contracts cover")
                        stopTimer()
                    }
                    is EventNewContract -> {
                        stopTimer()
                        d("refresh contract stations: ${it.current.name})")
                        // refresh current contract stations data
                        viewModelHome.getStationsFor(it.current)
                        startTimer()
                    }
                    is EventSameContract -> {
                        v("current contract has not changed")
                        // reload clustering
                        clusterStations?.cluster()
                    }
                    is EventStationList -> {
                        clusterStations?.clearItems()
                        //hideBottomSheet()
                        it.stations.map { station ->
                            clusterStations?.addItem(BICStationAnnotation(station))
                        }
                        clusterStations?.cluster()
                    }
                    is EventFailure -> {
                        viewModelHome.states.value?.apply {
                            when (this) {
                                is StateShowContracts -> {
                                    clusterContracts?.clearItems()
                                    hideBottomSheet()
                                    //TODO: display error message
                                }
                                is StateShowStations -> {
                                    clusterStations?.clearItems()
                                    hideBottomSheet()
                                    showErrorForCurrentContractStation()
                                }
                                else -> w("unexpected state")
                            }
                        }
                    }
                    else -> w("unexpected event")
                }
            }
        })
    }

    //endregion
}
