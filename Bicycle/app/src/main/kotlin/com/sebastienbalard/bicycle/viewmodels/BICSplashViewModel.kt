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

import com.sebastienbalard.bicycle.*
import com.sebastienbalard.bicycle.io.BicycleApi
import com.sebastienbalard.bicycle.io.dtos.BICConfigResponseDto
import com.sebastienbalard.bicycle.misc.BICSharedPreferences
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.android.schedulers.AndroidSchedulers

object StateConfig : State()

object StateContracts : State()

class BICSplashViewModel(private val bicycleApi: BicycleApi, private val contractRepository: BICContractRepository) : SBViewModel() {

    companion object : SBLog()

    fun loadConfig() {
        launch {
            _events.value = EventMessage(BICApplication.context.getString(R.string.bic_messages_info_config))
            bicycleApi.getConfig("media")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { response ->
                                setConfig(response)
                                _events.value = EventSuccess
                            },
                            { error -> _events.value = EventFailure(error) })
        }
    }

    fun loadAllContracts() {
        launch {
            contractRepository.loadAllContracts()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { event -> _events.value = event },
                            { error -> _events.value = EventFailure(error) },
                            { _events.value = EventSuccess })
        }
    }

    private fun setConfig(response: BICConfigResponseDto) {
        var delay = response.apps.android.version
        v("app check delay: $delay")
        BICSharedPreferences.appCheckDelay = delay
        delay = response.contracts.delay
        v("contracts check delay: $delay")
        BICSharedPreferences.contractsCheckDelay = delay
    }

}