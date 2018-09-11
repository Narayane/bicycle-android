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

package com.sebastienbalard.bicycle.views.settings

import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.SwitchPreferenceCompat
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.views.BICSplashActivity
import org.koin.android.ext.android.inject

open class BICSettingsFragment : PreferenceFragmentCompat() {

    companion object : SBLog()

    internal val crashReport: SBCrashReport by inject()
    internal val analytics: SBAnalytics by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.bic_preferences)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            PREFERENCE_CRASH_DATA_SENDING_PERMISSION -> {
                val isChecked = (preference as SwitchPreferenceCompat).isChecked
                i(crashReport.logMessage("[INFO]", "set crash data sending: $isChecked"))
                val bundle = Bundle()
                bundle.putInt("value", if (isChecked) 1 else 0)
                bundle.putInt("is_initial", 0)
                analytics.sendEvent("permission_crash_data_sending_set", bundle)
                true
            }
            PREFERENCE_USE_DATA_SENDING_PERMISSION -> {
                val isChecked = (preference as SwitchPreferenceCompat).isChecked
                i(crashReport.logMessage("[INFO]", "set use data sending: $isChecked"))
                val bundle = Bundle()
                bundle.putInt("value", if (isChecked) 1 else 0)
                bundle.putInt("is_initial", 0)
                analytics.sendEvent("permission_use_data_sending_set", bundle)
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }
}
