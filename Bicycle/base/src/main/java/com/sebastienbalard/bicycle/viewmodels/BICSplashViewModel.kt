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
import com.sebastienbalard.bicycle.SBLog
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import org.joda.time.DateTime
import org.joda.time.Days

object StateSplashConfig : SBState()
object StateSplashContracts : SBState()

object EventSplashConfigLoaded : SBEvent()
data class EventSplashLoadConfigFailed(val error: Throwable) : SBEvent()
object EventSplashCheckContracts : SBEvent()
data class EventSplashAvailableContracts(val count: Int) : SBEvent()

open class BICSplashViewModel(private val context: Context,
                              private val preferenceRepository: BICPreferenceRepository,
                              private val contractRepository: BICContractRepository) : SBViewModel() {

    companion object : SBLog()

    fun loadConfig() {
        _states.value = StateSplashConfig
        launch {
            preferenceRepository.loadConfig().subscribe({
                _events.value = EventSplashConfigLoaded
            }, { error ->
                _events.value = EventSplashLoadConfigFailed(error)
            })
        }
    }

    fun loadAllContracts() {
        _states.value = StateSplashContracts

        var timeToCheck = true
        val now = DateTime.now()

        preferenceRepository.contractsLastCheckDate?.let {
            v("last check: ${it.formatDate(context)}")
            timeToCheck = Days.daysBetween(it.toLocalDate(), now.toLocalDate()).days > BuildConfig
                    .DAYS_BETWEEN_CONTRACTS_CHECK
        }

        return if (timeToCheck) {
            d("get contracts from remote")
            _events.value = EventSplashCheckContracts
            launch {
                contractRepository.updateContracts()
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
}