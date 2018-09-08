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
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.sebastienbalard.bicycle.BuildConfig
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import io.fabric.sdk.android.Fabric

class SBCrashReport(context: Context, private val preferenceRepository: BICPreferenceRepository) {

    companion object : SBLog()

    init {
        if (BuildConfig.BUILD_TYPE == "release") {
            d("init crash report")
            Fabric.with(context, Crashlytics())
        }
    }

    fun setUserInformation(userid: String, username: String, email: String) {
        if (Fabric.isInitialized() && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.setUserIdentifier(userid)
            Crashlytics.setUserName(username)
            Crashlytics.setUserEmail(email)
        }
    }

    fun setCustomKey(key: String, value: String) {
        if (Fabric.isInitialized() && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.setString(key, value)
        }
    }

    fun logMessage(callerName: String, message: String) {
        if (Fabric.isInitialized() && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.log("$callerName: $message")
        }
    }

    fun catchException(throwable: Throwable) {
        if (Fabric.isInitialized() && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.logException(throwable)
        }
    }
}
