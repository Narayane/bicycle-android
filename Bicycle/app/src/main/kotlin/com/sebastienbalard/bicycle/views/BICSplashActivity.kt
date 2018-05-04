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
import com.sebastienbalard.bicycle.EventNextScreen
import com.sebastienbalard.bicycle.EventSuccess
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.SBActivity
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.viewmodels.BICSplashViewModel
import com.sebastienbalard.bicycle.views.home.BICHomeActivity
import kotlinx.android.synthetic.main.bic_activity_splash.*
import org.koin.android.architecture.ext.viewModel

class BICSplashActivity : SBActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICSplashActivity::class.java)
        }
    }

    private val viewModel: BICSplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_splash)
        v("onCreate")

        viewModel.events.observe(this, Observer { event ->
            event?.let {
                when (it) {
                    is EventSuccess -> {
                        v("event -> success: ${it.message}")
                        textViewEvent.text = it.message
                    }
                    is EventNextScreen -> {
                        v("event -> next screen")
                        startActivity(BICHomeActivity.getIntent(this))
                    }
                    else -> "" // error
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        v("onResume")
        viewModel.loadAllContracts()
    }
}
