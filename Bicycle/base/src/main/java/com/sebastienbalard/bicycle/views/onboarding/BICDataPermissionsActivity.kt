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

package com.sebastienbalard.bicycle.views.onboarding

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.SBActivity
import com.sebastienbalard.bicycle.SBLog
import com.sebastienbalard.bicycle.viewmodels.BICOnboardingViewModel
import com.sebastienbalard.bicycle.viewmodels.EventOnboardingDataPermissionsSet
import com.sebastienbalard.bicycle.views.home.BICHomeActivity
import kotlinx.android.synthetic.main.bic_activity_data_permissions.*
import org.koin.android.viewmodel.ext.android.viewModel

class BICDataPermissionsActivity : SBActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICDataPermissionsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
    }

    val viewModel: BICOnboardingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_data_permissions)
        v("onCreate")

        viewModel.events.observe(this, Observer { event ->
            event?.apply {
                v("event -> ${this::class.java.simpleName}")
                when (this) {
                    is EventOnboardingDataPermissionsSet -> {
                        runOnUiThread {
                            startActivity(BICHomeActivity.getIntent(this@BICDataPermissionsActivity))
                        }
                    }
                    else -> {}
                }
            }
        })

        buttonValidateDataPermissions.setOnClickListener {
            i("crash data permission: ${switchAllowCrashDataSending.isChecked}")
            i("use data permission: ${switchAllowUseDataSending.isChecked}")
            viewModel.saveDataPermissions(switchAllowCrashDataSending.isChecked, switchAllowUseDataSending.isChecked)
        }
    }
}
