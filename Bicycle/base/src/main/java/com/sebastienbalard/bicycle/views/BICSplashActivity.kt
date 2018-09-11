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

package com.sebastienbalard.bicycle.views

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.analytics.FirebaseAnalytics
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.viewmodels.*
import com.sebastienbalard.bicycle.views.home.BICHomeActivity
import com.sebastienbalard.bicycle.views.onboarding.BICDataPermissionsActivity
import kotlinx.android.synthetic.main.bic_activity_splash.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class BICSplashActivity : SBActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICSplashActivity::class.java)
        }
    }

    internal val viewModel: BICSplashViewModel by viewModel()
    internal val crashReport: SBCrashReport by inject()
    internal val analytics: SBAnalytics by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_splash)
        v("onCreate")
        analytics.sendEvent("app_open")

        viewModel.states.observe(this, Observer { state ->
            state?.let {
                v("state -> ${it::class.java.simpleName}")
                when (it) {
                    is StateSplashConfig -> textViewTitle.text = getString(R.string.bic_messages_info_init)
                    is StateSplashContracts -> textViewTitle.text = getString(R.string.bic_messages_info_config)
                    else -> {}
                }
            }
        })

        viewModel.events.observe(this, Observer { event ->
            event?.apply {
                v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventSplashConfigLoaded, is EventSplashLoadConfigFailed -> {
                        viewModel.loadAllContracts()
                    }
                    is EventSplashCheckContracts -> {
                        textViewSubtitle.text = getString(R.string.bic_messages_info_check_contracts_data_version)
                    }
                    is EventSplashAvailableContracts -> {
                        textViewSubtitle.text = getString(R.string.bic_messages_info_contracts_loaded, count)
                        i(crashReport.logMessage("[INFO]", "load $count contracts"))
                        viewModel.requestDataSendingPermissions()
                    }
                    is EventSplashRequestDataPermissions -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (needed or BuildConfig.DEBUG) {
                                startActivity(BICDataPermissionsActivity.getIntent(this@BICSplashActivity))
                            } else {
                                startActivity(BICHomeActivity.getIntent(this@BICSplashActivity))
                            }
                        }, 2000)
                    }
                    else -> {}
                }
            }
        })

        viewModel.loadConfig()
    }
}
