package com.sebastienbalard.bicycle.repositories

import android.content.SharedPreferences
import com.sebastienbalard.bicycle.EventFailure
import com.sebastienbalard.bicycle.EventSuccess
import com.sebastienbalard.bicycle.io.BicycleApi
import com.sebastienbalard.bicycle.io.dtos.BICConfigResponseDto
import com.sebastienbalard.bicycle.misc.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.joda.time.DateTime

open class BICPreferenceRepository(private val bicycleApi: BicycleApi, private val preferences: SharedPreferences) {

    companion object : SBLog()

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
            bicycleApi.getConfig("media")
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
        var delay = response.apps.android.version
        v("app check delay: $delay")
        appCheckDelay = delay
        delay = response.contracts.delay
        v("contracts check delay: $delay")
        contractsCheckDelay = delay
    }
}