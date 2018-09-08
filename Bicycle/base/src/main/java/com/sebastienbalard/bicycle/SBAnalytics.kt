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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.firebase.analytics.FirebaseAnalytics
import com.sebastienbalard.bicycle.BuildConfig
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import io.fabric.sdk.android.Fabric

open class SBAnalytics(context: Context, private val preferenceRepository: BICPreferenceRepository) {

    companion object : SBLog()

    private var analytics: FirebaseAnalytics? = null

    init {
        if (BuildConfig.BUILD_TYPE == "release") {
            v("init analytics")
            analytics = FirebaseAnalytics.getInstance(context)
        }
    }

    open fun sendEvent(name: String, bundle: Bundle? = null) {
        analytics?.apply {
            if (preferenceRepository.isUseDataSendingAllowed) {
                d("log analytics event: $name")
                logEvent(name, bundle)
            }
        }
    }
}
