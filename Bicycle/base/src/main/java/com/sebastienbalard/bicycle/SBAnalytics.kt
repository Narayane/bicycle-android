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
import io.fabric.sdk.android.Fabric

class SBAnalytics(context: Context, private val preferences: SharedPreferences) {

    init {
        Fabric.with(context, Crashlytics())
        //Fabric.with(context, Answers())
    }

    fun setUserInformation(userid: String, username: String, email: String) {
        if (BuildConfig.DEBUG && Fabric.isInitialized()) {
            Crashlytics.setUserIdentifier(userid)
            Crashlytics.setUserName(username)
            Crashlytics.setUserEmail(email)
        }
    }

    fun setCustomKey(key: String, value: String) {
        if (BuildConfig.DEBUG) {
            Crashlytics.setString(key, value)
        }
    }

    fun logMessage(callerName: String, message: String) {
        if (BuildConfig.DEBUG && Fabric.isInitialized()) {
            Crashlytics.log("$callerName: $message")
        }
    }

    fun catchException(throwable: Throwable) {
        if (BuildConfig.DEBUG && Fabric.isInitialized()) {
            Crashlytics.logException(throwable)
        }
    }

    fun logEvent(name: String) {
        if (BuildConfig.DEBUG && Fabric.isInitialized()) {
            Answers.getInstance().logCustom(CustomEvent(name))
        }
    }
}
