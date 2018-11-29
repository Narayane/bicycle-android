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
import com.crashlytics.android.Crashlytics
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import io.fabric.sdk.android.Fabric

open class SBCrashReport(context: Context, private val preferenceRepository: BICPreferenceRepository) {

    companion object : SBLog()

    init {
        if (BuildConfig.BUILD_TYPE == "release") {
            i("init crash report")
            Fabric.with(context, Crashlytics())
        }
    }

    open fun setUserInformation(userId: String, userName: String, email: String) {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.setUserIdentifier(userId)
            Crashlytics.setUserName(userName)
            Crashlytics.setUserEmail(email)
        }
    }

    open fun setCustomKey(key: String, value: String) {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.setString(key, value)
        }
    }

    open fun logDebug(callerName: String, message: String): String {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.log("[DEBUG] | $callerName: $message")
        }
        return message
    }

    open fun logInfo(callerName: String, message: String): String {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.log("[INFO] | $callerName: $message")
        }
        return message
    }

    open fun logWarning(callerName: String, message: String): String {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.log("[WARN] | $callerName: $message")
        }
        return message
    }

    open fun logError(callerName: String, message: String): String {
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.log("[ERROR] | $callerName: $message")
        }
        return message
    }

    open fun catchException(callerName: String, message: String, exception: Exception): String  {
        exception.cause?.let { throwable ->
            return catchException(callerName, message, throwable)
        }
        return message
    }

    open fun catchException(callerName: String, message: String, throwable: Throwable): String  {
        logError(callerName, message)
        if (BuildConfig.BUILD_TYPE == "release" && preferenceRepository.isCrashDataSendingAllowed) {
            Crashlytics.logException(throwable)
        }
        return message
    }

    open fun crash(): String {
        if (BuildConfig.BUILD_TYPE == "release") {
            Crashlytics.getInstance().crash()
        }
        return "force crash"
    }
}
