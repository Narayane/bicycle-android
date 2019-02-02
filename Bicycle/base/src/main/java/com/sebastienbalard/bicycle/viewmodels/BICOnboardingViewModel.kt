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

package com.sebastienbalard.bicycle.viewmodels

import android.os.Bundle
import com.sebastienbalard.bicycle.SBAnalytics
import com.sebastienbalard.bicycle.SBEvent
import com.sebastienbalard.bicycle.SBLog
import com.sebastienbalard.bicycle.SBViewModel
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository

data class EventDataSendingPermissionsLoaded(val allowCrashDataSending: Boolean, val allowUseDataSending: Boolean) : SBEvent()
object EventDataSendingPermissionsSet : SBEvent()

open class BICOnboardingViewModel(private val preferenceRepository: BICPreferenceRepository, private val analytics: SBAnalytics) : SBViewModel() {

    companion object : SBLog()

    open fun loadDataSendingPermissions() {
        _events.value = EventDataSendingPermissionsLoaded(preferenceRepository.isCrashDataSendingAllowed,
                preferenceRepository.isUseDataSendingAllowed)
    }

    open fun saveDataSendingPermissions(allowCrashDataSending: Boolean, allowUseDataSending: Boolean) {
        preferenceRepository.isCrashDataSendingAllowed = allowCrashDataSending
        preferenceRepository.isUseDataSendingAllowed = allowUseDataSending
        preferenceRepository.requestDataSendingPermissions = false
        var bundle = Bundle()
        bundle.putInt("allowed", if (allowCrashDataSending) 1 else 0)
        bundle.putInt("is_onboarding", 1)
        analytics.sendEvent("crash_data_sending", bundle)
        bundle = Bundle()

        bundle.putInt("allowed", if (allowUseDataSending) 1 else 0)
        bundle.putInt("is_onboarding", 1)
        analytics.sendEvent("use_data_sending", bundle)
        _events.value = EventDataSendingPermissionsSet
    }
}