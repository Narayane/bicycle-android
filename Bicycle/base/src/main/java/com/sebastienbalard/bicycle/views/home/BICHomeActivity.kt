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
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.viewmodels.*
import com.sebastienbalard.bicycle.views.BICAboutActivity
import com.sebastienbalard.bicycle.views.BICHelpActivity
import kotlinx.android.synthetic.main.bic_activity_home.*
import kotlinx.android.synthetic.main.sb_widget_appbar.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.run
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.concurrent.timerTask

class BICHomeActivity : SBMapActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICHomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent
                    .FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    private val viewModelHome: BICHomeViewModel by viewModel()

    private var clusterManager: ClusterManager<BICStationAnnotation>? = null
    private var listContractsAnnotations: MutableList<Marker>? = null
    private var timer: Timer? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    private var selectedMarker: Marker? = null

    //region Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_home)
        v("onCreate")
        initToolbar()

        listContractsAnnotations = mutableListOf()

        initMap(savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        viewModelHome.states.observe(this, Observer { state ->
            state?.let {
                when (it) {
                    is StateShowContracts -> hideBottomSheet()
                    is StateShowStations -> hideBottomSheet()
                    else -> w("unexpected case")
                }
            }
        })
        viewModelHome.events.observe(this, Observer { event ->
            event?.let {
                when (it) {
                    is EventContractList -> createAnnotationsFor(it.contracts)
                    is EventOutOfAnyContract -> {
                        d("current bounds is out of contracts cover")
                        stopTimer()
                    }
                    is EventNewContract -> {
                        stopTimer()
                        d("refresh contract stations: ${it.current.name} (${it.current.provider.tag})")
                        // refresh current contract stations data
                        viewModelHome.getStationsFor(it.current)
                        startTimer()
                    }
                    is EventSameContract -> {
                        v("current contract has not changed")
                        // reload clustering
                        clusterManager?.cluster()
                    }
                    is EventStationList -> {
                        clusterManager?.clearItems()
                        hideBottomSheet()
                        it.stations.map { station ->
                            clusterManager?.addItem(BICStationAnnotation(station))
                        }
                        clusterManager?.cluster()
                    }
                    is EventFailure -> {
                        clusterManager?.clearItems()
                        hideBottomSheet()
                        showErrorForCurrentContractStation()
                    }
                    else -> w("unexpected case")
                }
            }
        })
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
            R.id.bic_menu_home_item_help -> {
                i("click on menu item: help")
                startActivity(BICHelpActivity.getIntent(this))
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
        clusterManager = ClusterManager(this, googleMap!!)
        clusterManager?.renderer = BICStationAnnotation.Renderer(this, googleMap!!, clusterManager!!)
        googleMap!!.setOnInfoWindowClickListener(clusterManager)
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
        selectedMarker?.let { currentMarker ->
            if (currentMarker == marker) {
                return // same marker clicked
            } else {
                unselectedMarker(currentMarker)
            }
        }
        marker.snippet?.apply {
            if (startsWith("type=")) {
                if (endsWith("station")) {
                    (marker.tag as? BICStation)?.apply {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconSelected))
                        textViewBottomSheetTitle.text = name
                    }
                } else if (endsWith("contract")) {
                    (marker.tag as? BICContract)?.apply {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconSelected))
                        textViewBottomSheetTitle.text = name
                    }
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                selectedMarker = marker
            }
        }
    }

    override fun onMapClicked() {
        selectedMarker?.let { currentMarker ->
            unselectedMarker(currentMarker)
        }
    }

    override fun onCameraIdle() {
        refreshMarkers()
    }

    //endregion

    //region Private methods

    private fun unselectedMarker(marker: Marker) {
        marker.snippet?.apply {
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
        hideBottomSheet()
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        selectedMarker = null
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
                    d("refresh contract stations: ${it.name} (${it.provider.tag})")
                    viewModelHome.refreshStationsFor(it)
                }
            }, delay, delay)
        }
    }

    private fun stopTimer() {
        timer?.let {
            d("stop timer")
            it.cancel()
            timer = null
        }
    }

    private fun refreshMarkers() {
        val level = googleMap?.cameraPosition?.zoom?.toInt()
        level?.let {
            d("current zoom level: $level")
            if (it >= 10) {
                deleteContractsAnnotations()
                viewModelHome.determineCurrentContract(googleMap!!.projection.visibleRegion.latLngBounds)
            } else {
                stopTimer()
                viewModelHome.getAllContracts()
            }
        }
    }

    private fun deleteContractsAnnotations() {
        listContractsAnnotations?.let {
            if (it.size > 0) {
                d("delete contracts annotations")
                it.map { marker -> marker.remove() }
                it.clear()
            }
        }
    }

    private fun createAnnotationsFor(contracts: List<BICContract>) {
        val hasMarkers = clusterManager?.markerCollection?.markers?.isNotEmpty()?.or(false)!!
        val hasClusterMarkers = clusterManager?.clusterMarkerCollection?.markers?.isNotEmpty()?.or(false)!!
        if (hasMarkers || hasClusterMarkers) {
            clusterManager?.clearItems()
            clusterManager?.cluster()
        }
        if (listContractsAnnotations?.size == 0) {
            d("create contracts annotations")
            async(CommonPool) {
                var options: MarkerOptions
                contracts.map { contract ->
                    options = MarkerOptions()
                    options.position(contract.center)
                    options.icon(BitmapDescriptorFactory.fromBitmap(contract.icon))
                    options.snippet("type=contract")
                    run(UI) {
                        googleMap?.addMarker(options)?.let { marker ->
                            listContractsAnnotations?.add(marker)
                            marker.tag = contract
                        }
                    }
                }
                run(UI) {
                    // do this to avoid contracts partial display after activity launch
                    val level = googleMap?.cameraPosition?.zoom?.toInt()
                    level?.let {
                        if (it >= 10) {
                            deleteContractsAnnotations()
                        }
                    }
                }
            }
        }
    }

    //endregion
}
