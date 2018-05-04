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

import com.sebastienbalard.bicycle.EventFailure
import com.sebastienbalard.bicycle.EventNextScreen
import com.sebastienbalard.bicycle.SBViewModel
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import io.reactivex.android.schedulers.AndroidSchedulers

class BICSplashViewModel(private val contractRepository: BICContractRepository) : SBViewModel() {

    companion object : SBLog()

    fun loadAllContracts() {
        launch {
            contractRepository.loadAllContracts()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { event -> _events.value = event },
                            { error -> _events.value = EventFailure(error) },
                            { _events.value = EventNextScreen })
        }
    }

}