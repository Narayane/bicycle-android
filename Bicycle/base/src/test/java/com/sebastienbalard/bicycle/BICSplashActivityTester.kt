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

import android.arch.lifecycle.MutableLiveData
import android.view.View
import com.sebastienbalard.bicycle.viewmodels.EventSplashConfigLoaded
import com.sebastienbalard.bicycle.viewmodels.StateSplashConfig
import com.sebastienbalard.bicycle.views.BICSplashActivity
import kotlinx.android.synthetic.main.bic_activity_splash.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.StandAloneContext.stopKoin
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class)
class BICSplashActivityTester {

    private lateinit var activity: BICSplashActivity
    private lateinit var controller: ActivityController<BICSplashActivity>

    @Test
    fun testOnCreate() {

        val state = MutableLiveData<SBState>()
        state.postValue(StateSplashConfig)
        `when`(activity.viewModel.states).thenReturn(state)

        val events = MutableLiveData<SBEvent>()
        events.postValue(EventSplashConfigLoaded)
        `when`(activity.viewModel.events).thenReturn(events)

        assertThat(activity, notNullValue())
        assertThat(activity.viewModel, notNullValue())
        assertThat(activity.viewModel.states, notNullValue())

        verify(activity.viewModel).loadConfig()

        assertThat(activity.textViewTitle, notNullValue())
        assertThat(activity.textViewTitle.visibility, `is`(equalTo(View.VISIBLE)))
        //assertThat(activity!!.textViewTitle.text, `is`(equalTo(RuntimeEnvironment.application.getString(R.string.bic_messages_info_init))))
        assertThat(activity.textViewSubtitle, notNullValue())
        assertThat(activity.textViewSubtitle.visibility, `is`(equalTo(View.VISIBLE)))
        //assertThat(activity!!.textViewSubtitle.text, `is`(equalTo("")))
    }

    @Before
    fun setUp() {
        loadKoinModules(commonTestModule)
        controller = Robolectric.buildActivity(
                BICSplashActivity::class.java,
                BICSplashActivity.getIntent(RuntimeEnvironment.application.applicationContext))
        activity = spy(controller.get())
        `when`(activity.viewModel.states).thenReturn(MutableLiveData<SBState>())
        `when`(activity.viewModel.events).thenReturn(MutableLiveData<SBEvent>())
        activity = spy(controller.create().start().resume().visible().get())
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}