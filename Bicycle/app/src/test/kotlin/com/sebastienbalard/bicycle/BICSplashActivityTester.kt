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
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class, constants = BuildConfig::class/*, shadows = [BICShadowDrawableResourcesImpl::class], sdk = [27]*/)
class BICSplashActivityTester {

    private var activity: BICSplashActivity? = null

    @Test
    fun testOnCreate() {

        val state = MutableLiveData<State>()
        state.postValue(StateSplashConfig)
        `when`(activity!!.viewModel.states).thenReturn(state)

        val events = MutableLiveData<Event>()
        events.postValue(EventSplashConfigLoaded)
        `when`(activity!!.viewModel.events).thenReturn(events)

        assertThat(activity, notNullValue())
        assertThat(activity!!.viewModel, notNullValue())
        assertThat(activity!!.viewModel.states, notNullValue())

        verify(activity!!.viewModel).loadConfig()

        assertThat(activity!!.textViewTitle, notNullValue())
        assertThat(activity!!.textViewTitle.visibility, `is`(equalTo(View.VISIBLE)))
        //assertThat(activity!!.textViewTitle.text, `is`(equalTo(RuntimeEnvironment.application.getString(R.string.bic_messages_info_init))))
        assertThat(activity!!.textViewSubtitle, notNullValue())
        assertThat(activity!!.textViewSubtitle.visibility, `is`(equalTo(View.VISIBLE)))
        //assertThat(activity!!.textViewSubtitle.text, `is`(equalTo("")))
    }

    @Before
    fun setUp() {
        loadKoinModules(commonTestModule)
        activity = Robolectric.setupActivity(BICSplashActivity::class.java)
    }

    @After
    fun tearDown() {
        activity?.finish()
        stopKoin()
    }
}