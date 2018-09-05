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

package com.sebastienbalard.bicycle.repositories

import android.content.SharedPreferences
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.io.BicycleDataSource
import com.sebastienbalard.bicycle.io.dtos.BICConfigResponseDto
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.joda.time.DateTime

open class BICPreferenceRepository(private val bicycleDataSource: BicycleDataSource, private val preferences: SharedPreferences) {

    companion object : SBLog()

    open var requestDataSendingPermissions: Boolean
        get() {
            return preferences.getBoolean(PREFERENCE_REQUEST_DATA_SENDING_PERMISSIONS, true)
        }
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_REQUEST_DATA_SENDING_PERMISSIONS, value).commit()
        }

    open var isCrashDataSendingAllowed: Boolean
        get() {
            return preferences.getBoolean(PREFERENCE_CRASH_DATA_SENDING_PERMISSION, true)
        }
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_CRASH_DATA_SENDING_PERMISSION, value).commit()
        }

    open var isUseDataSendingAllowed: Boolean
        get() {
            return preferences.getBoolean(PREFERENCE_USE_DATA_SENDING_PERMISSION, true)
        }
        set(value) {
            preferences.edit().putBoolean(PREFERENCE_USE_DATA_SENDING_PERMISSION, value).commit()
        }

    open var appCheckDelay: Int
        get() {
            return preferences.getInt(PREFERENCE_APP_CHECK_DELAY, 7)
        }
        set(value) {
            preferences.edit().putInt(PREFERENCE_APP_CHECK_DELAY, value).commit()
        }

    open var contractsCheckDelay: Int
        get() {
            return preferences.getInt(PREFERENCE_CONTRACTS_CHECK_DELAY, 30)
        }
        set(value) {
            preferences.edit().putInt(PREFERENCE_CONTRACTS_CHECK_DELAY, value).commit()
        }

    open var contractsLastCheckDate: DateTime?
        get() {
            val millis = preferences.getLong(PREFERENCE_CONTRACTS_LAST_CHECK_DATE, 0)
            return if (millis != 0L) DateTime(millis) else null
        }
        set(value) {
            value?.let {
                preferences.edit().putLong(PREFERENCE_CONTRACTS_LAST_CHECK_DATE, it.toDate().time).commit()
            }
        }

    open var contractsVersion: Int
        get() {
            return preferences.getInt(PREFERENCE_CONTRACTS_VERSION, 0)
        }
        set(value) {
            preferences.edit().putInt(PREFERENCE_CONTRACTS_VERSION, value).commit()
        }

    open fun loadConfig(): Completable {
        return Completable.create { observer ->
            bicycleDataSource.getConfig()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        setConfig(response)
                        observer.onComplete()
                    }, { error ->
                       observer.onError(error)
                    })
        }
    }

    private fun setConfig(response: BICConfigResponseDto) {
        var delay = response.apps.delay
        v("app check delay: $delay")
        appCheckDelay = delay
        delay = response.contracts.delay
        v("contracts check delay: $delay")
        contractsCheckDelay = delay
    }
}