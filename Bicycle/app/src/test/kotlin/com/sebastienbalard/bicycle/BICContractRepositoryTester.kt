package com.sebastienbalard.bicycle

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.sebastienbalard.bicycle.data.BICContractDao
import com.sebastienbalard.bicycle.io.BicycleApi
import com.sebastienbalard.bicycle.repositories.BICContractRepository
import com.sebastienbalard.bicycle.repositories.BICPreferenceRepository
import io.reactivex.Single
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = BICTestApplication::class, constants = BuildConfig::class)
class BICContractRepositoryTester {

    @Rule
    @JvmField
    val execRule = InstantTaskExecutorRule()

    private val mockBicycleApi = mock(BicycleApi::class.java)
    private val mockContractDao = mock(BICContractDao::class.java)
    private val mockPreferenceRepository = mock(BICPreferenceRepository::class.java)

    private var repository: BICContractRepository? = null

    @Test
    fun testInit() {
        assertThat(repository, notNullValue())
    }

    @Test
    fun testUpdateContracts() {

        //`when`(mockBicycleApi.getContractsData("media")).thenReturn(Single.just(432))

        val spy = repository!!.updateContracts().test()

        verify(mockBicycleApi).getContractsData("media")
    }

    @Before
    fun setUp() {
        repository = BICContractRepository(mockBicycleApi, mockContractDao, mockPreferenceRepository)
    }

    @After
    fun tearDown() {
        repository = null
    }
}