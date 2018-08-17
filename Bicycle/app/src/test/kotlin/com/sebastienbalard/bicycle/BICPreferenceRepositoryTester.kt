package com.sebastienbalard.bicycle

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.SharedPreferences
import com.sebastienbalard.bicycle.data.BICContractDao
import com.sebastienbalard.bicycle.io.BicycleApi
import com.sebastienbalard.bicycle.io.dtos.BICConfigAndroidDto
import com.sebastienbalard.bicycle.io.dtos.BICConfigAppsDto
import com.sebastienbalard.bicycle.io.dtos.BICConfigContractsDto
import com.sebastienbalard.bicycle.io.dtos.BICConfigResponseDto
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import io.reactivex.Single
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class, constants = BuildConfig::class)
class BICPreferenceRepositoryTester {

    @Rule
    @JvmField
    val execRule = InstantTaskExecutorRule()

    private val mockBicycleApi = mock(BicycleApi::class.java)
    private val mockSharedPreferences = mock(SharedPreferences::class.java)

    private var repository: BICPreferenceRepository? = null

    @Test
    fun testInit() {
        assertThat(repository, notNullValue())
    }

    @Test
    fun testLoadConfig() {

        val response = BICConfigResponseDto(BICConfigAppsDto(14, BICConfigAndroidDto("1.0", false)), BICConfigContractsDto(30))

        `when`(mockBicycleApi.getConfig("media")).thenReturn(Single.just(response))

        val spy = repository!!.loadConfig().test()

        verify(mockBicycleApi).getConfig("media")

        //Assert.assertThat(repository!!.appCheckDelay, `is`(equalTo(14)))
        //Assert.assertThat(repository!!.contractsCheckDelay, `is`(equalTo(30)))
    }

    @Before
    fun setUp() {
        repository = BICPreferenceRepository(mockBicycleApi, mockSharedPreferences)
    }

    @After
    fun tearDown() {
        repository = null
    }
}