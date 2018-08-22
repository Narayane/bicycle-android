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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sebastienbalard.bicycle.SBViewModel
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.models.BICPlace
import com.sebastienbalard.bicycle.repositories.BICContractRepository

class BICSearchViewModel(private val contractRepository: BICContractRepository) : SBViewModel() {

    companion object : SBLog()

    var isSearchButtonEnabled = MutableLiveData<Boolean>()

    var departure: BICPlace? = null
        set(value) {
            field = value
            field?.let {
                it.contract = contractRepository.getContractBy(it.location)
                v("departure place contract is ${it.contract?.name}")
            }
            isSearchButtonEnabled.value = departure != null && arrival != null
        }
    var arrival: BICPlace? = null
        set(value) {
            field = value
            field?.let {
                it.contract = contractRepository.getContractBy(it.location)
                v("arrival place contract is ${it.contract?.name}")
            }
            isSearchButtonEnabled.value = departure != null && arrival != null
        }
    var bikesCount: Int = 1
    var freeSlotsCount: Int = 1

    val isComplete: Boolean
        get() = departure?.contract != null && arrival?.contract != null
                && (departure!!.contract!!.center == arrival!!.contract!!.center)

}
