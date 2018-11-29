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

import android.content.Context
import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.extensions.formatDate
import com.sebastienbalard.bicycle.extensions.toUTC
import com.sebastienbalard.bicycle.io.dtos.BICConfigAndroidDto
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

object StateSplashConfig : SBState()
object StateSplashContracts : SBState()

object EventSplashForceUpdate : SBEvent()
object EventSplashConfigLoaded : SBEvent()
data class EventSplashLoadConfigFailed(val error: Throwable) : SBEvent()
object EventSplashCheckContracts : SBEvent()
data class EventSplashAvailableContracts(val count: Int) : SBEvent()
data class EventSplashRequestDataPermissions(val needed: Boolean) : SBEvent()

open class BICSplashViewModel(private val context: Context,
                              private val preferenceRepository: BICPreferenceRepository,
                              private val contractRepository: BICContractRepository) : SBViewModel() {

    companion object : SBLog()

    open fun loadConfig() {
        _states.value = StateSplashConfig
        launch {
            preferenceRepository.loadConfig().subscribe({ androidConfig ->
                if (checkForceUpdate(androidConfig)) {
                    _events.value = EventSplashForceUpdate
                } else {
                    _events.value = EventSplashConfigLoaded
                }
            }, { error ->
                _events.value = EventSplashLoadConfigFailed(error)
            })
        }
    }

    open fun loadAllContracts(hasConnectivity: Boolean) {
        _states.value = StateSplashContracts

        var timeToCheck = true
        val now = LocalDateTime.now().toUTC()

        preferenceRepository.contractsLastCheckDate?.let { datetime ->
            v("contracts last check: ${datetime.formatDate(context)}")
            timeToCheck = ChronoUnit.DAYS.between(datetime.toLocalDate(), now.toLocalDate()) > preferenceRepository.contractsCheckDelay
        }

        return if (timeToCheck) {
            d("get contracts from remote")
            _events.value = EventSplashCheckContracts
            launch {
                contractRepository.updateContracts(hasConnectivity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ count ->
                            _events.value = EventSplashAvailableContracts(count)
                        }, { _ ->

                        })
            }
        } else {
            d("contracts are up-to-date")
            preferenceRepository.contractsLastCheckDate = now
            launch {
                contractRepository.getContractCount()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ count ->
                            _events.value = EventSplashAvailableContracts(count)
                        }, { _ ->

                        })
            }
        }
    }

    open fun requestDataSendingPermissions() {
        _events.value = EventSplashRequestDataPermissions(preferenceRepository.requestDataSendingPermissions)
    }

    private fun checkForceUpdate(androidConfig: BICConfigAndroidDto): Boolean {

        var timeToCheck = true
        val now = LocalDateTime.now().toUTC()

        preferenceRepository.appLastCheckDate?.let { datetime ->
            v("app last check: ${datetime.formatDate(context)}")
            timeToCheck = ChronoUnit.DAYS.between(datetime.toLocalDate(), now.toLocalDate()) > preferenceRepository.contractsCheckDelay
        }

        if (timeToCheck) {
            d("check app version")

            val lastVersion = DefaultArtifactVersion(androidConfig.version)
            d("last version: $lastVersion")
            val currentVersion = DefaultArtifactVersion(BuildConfig.VERSION_NAME)
            d("current version: $currentVersion")

            if (currentVersion.compareTo(lastVersion) == -1) {
                return androidConfig.forceUpdate
            } else {
                d("app is up-to-date")
                preferenceRepository.appLastCheckDate = LocalDateTime.now().toUTC()
            }
        } else {
            v("no need to check again")
        }
        return false
    }
}