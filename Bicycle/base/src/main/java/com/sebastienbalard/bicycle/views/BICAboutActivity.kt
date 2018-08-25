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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sebastienbalard.bicycle.BuildConfig
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.SBActivity
import com.sebastienbalard.bicycle.SBLog
import kotlinx.android.synthetic.main.bic_activity_about.*

class BICAboutActivity : SBActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICAboutActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_about)
        v("onCreate")
        initToolbar()
        textViewAppVersion.text = getString(R.string.bic_app_version_label, BuildConfig.VERSION_NAME, BuildConfig
                .VERSION_CODE)
    }
}
