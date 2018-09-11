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

package com.sebastienbalard.bicycle

import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.text.TextUtils
import com.jakewharton.threetenabp.AndroidThreeTen
import com.sebastienbalard.bicycle.data.BICContract
import com.sebastienbalard.bicycle.di.bicycleApp
import com.sebastienbalard.bicycle.models.BICStation
import com.sebastienbalard.bicycle.views.home.BICHomeActivity
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin

open class BICApplication : Application() {

    companion object: SBLog()

    internal val crashReport: SBCrashReport by inject()

    protected var _hasConnectivity: Boolean = true
    open val hasConnectivity: Boolean
        get() = _hasConnectivity

    override fun onCreate() {
        super.onCreate()
        v("onCreate")
        startKoin(this, bicycleApp)
        AndroidThreeTen.init(applicationContext)
        BICContract.initConstants(applicationContext)
        BICStation.initConstants(applicationContext)
        watchConnectivity()
    }

    //region Private methods
    private fun watchConnectivity() {
        v("watch connectivity")
        registerReceiver(broadcastReceiver, IntentFilter(NOTIFICATION_CONNECTIVITY_ACTION))
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        registerComponentCallbacks(callbacks)
        val isConnected = connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
        sendBroadcast(Intent(NOTIFICATION_CONNECTIVITY_ACTION).putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, !(isConnected)))
    }

    private fun unwatchConnectivity() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.apply {
                if (TextUtils.equals(action, NOTIFICATION_CONNECTIVITY_ACTION)) {
                    v("onReceive")
                    _hasConnectivity = !(intent.extras?.getBoolean(android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) ?: false)
                    i(crashReport.logMessage("[INFO]", "has connectivity: $hasConnectivity"))
                }
            }
        }
    }

    private val callbacks = object : ComponentCallbacks2 {
        override fun onLowMemory() {

        }

        override fun onConfigurationChanged(configuration: Configuration?) {

        }

        @SuppressLint("SwitchIntDef")
        override fun onTrimMemory(level: Int) {
            when (level) {
                ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> { // app will be killed soon
                    d("app will be killed")
                    unwatchConnectivity()
                }
                else -> {}
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            sendBroadcast(Intent(NOTIFICATION_CONNECTIVITY_ACTION).putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))
        }

        override fun onLost(network: Network) {
            sendBroadcast(Intent(NOTIFICATION_CONNECTIVITY_ACTION).putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true))
        }
    }
    //endregion
}