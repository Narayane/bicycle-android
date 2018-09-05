/**
 * Copyright © 2017 Bicycle (Sébastien BALARD)
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

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.sb_widget_appbar.*

open class SBActivity : AppCompatActivity() {

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0,0)
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.sb_anim_activity_enter, R.anim.sb_anim_activity_exit)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun launch(rx: () -> Disposable) {
        disposables.add(rx())
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    protected fun dispose(disposable: Disposable) {
        disposables.add(disposable)
    }

    protected open fun initToolbar() {
        setSupportActionBar(toolbar)
        try {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } catch (exception: NullPointerException) {
            // do nothing
        }
    }
}