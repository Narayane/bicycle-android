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

package com.sebastienbalard.bicycle.views

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sebastienbalard.bicycle.EventMessage
import com.sebastienbalard.bicycle.EventSuccess
import com.sebastienbalard.bicycle.R
import com.sebastienbalard.bicycle.SBActivity
import com.sebastienbalard.bicycle.misc.SBLog
import com.sebastienbalard.bicycle.viewmodels.BICSplashViewModel
import com.sebastienbalard.bicycle.viewmodels.StateConfig
import com.sebastienbalard.bicycle.viewmodels.StateContracts
import com.sebastienbalard.bicycle.views.home.BICHomeActivity
import kotlinx.android.synthetic.main.bic_activity_splash.*
import org.koin.android.viewmodel.ext.android.viewModel

class BICSplashActivity : SBActivity() {

    companion object : SBLog() {
        fun getIntent(context: Context): Intent {
            return Intent(context, BICSplashActivity::class.java)
        }
    }

    private val viewModel: BICSplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bic_activity_splash)
        v("onCreate")

        viewModel.states.observe(this, Observer { state ->
            state?.let {
                v("state -> ${it::class.java}")
                when (it) {
                    is StateConfig -> ""
                    is StateContracts -> ""
                    else -> startActivity(BICHomeActivity.getIntent(this))
                }
            }
        })

        viewModel.events.observe(this, Observer { event ->
            event?.let {
                when (it) {
                    is EventMessage -> {
                        v("event -> success: ${it.message}")
                        textViewEvent.text = it.message
                        viewModel.loadAllContracts()
                    }
                    is EventSuccess -> {
                        v("event -> next screen")
                        startActivity(BICHomeActivity.getIntent(this))
                        this@BICSplashActivity.finish()
                    }
                    else -> "" // error
                }
            }
        })

        viewModel.loadConfig()
    }

    /*val asyncJobs: MutableList<Job> = mutableListOf()

    fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        val job: Job = kotlinx.coroutines.experimental.launch(UI) { block() }
        asyncJobs.add(job)
        job.invokeOnCompletion { asyncJobs.remove(job) }
    }

    /*fun cancelAllAsync() {
        val asyncJobsSize = asyncJobs.size

        if (asyncJobsSize > 0) {
            for (i in asyncJobsSize - 1 downTo 0) {
                asyncJobs[i].cancel()
            }
        }
    }*/

    val deferredObjects: MutableList<Deferred<*>> = mutableListOf()

    suspend fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        val deferred: Deferred<T> = kotlinx.coroutines.experimental.async(CommonPool) { block() }
        deferredObjects.add(deferred)
        deferred.invokeOnCompletion { deferredObjects.remove(deferred) }
        return deferred
    }

    suspend fun <T> asyncAwait(block: suspend CoroutineScope.() -> T): T {
        return async(block).await()
    }

    fun cancelAllAsync() {
        val deferredObjectsSize = deferredObjects.size

        if (deferredObjectsSize > 0) {
            for (i in deferredObjectsSize - 1 downTo 0) {
                deferredObjects[i].cancel()
            }
        }
    }

    suspend fun CoroutineScope.tryCatch(
            tryBlock: suspend CoroutineScope.() -> Unit,
            catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
            handleCancellationExceptionManually: Boolean = false) {
        try {
            tryBlock()
        } catch (e: Throwable) {
            if (e !is CancellationException ||
                    handleCancellationExceptionManually) {
                catchBlock(e)
            } else {
                throw e
            }
        }
    }*/
}
