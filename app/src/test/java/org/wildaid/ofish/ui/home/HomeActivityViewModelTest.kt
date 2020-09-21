package org.wildaid.ofish.ui.home

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.R
import org.wildaid.ofish.data.OFF_DUTY
import org.wildaid.ofish.data.ON_DUTY
import org.wildaid.ofish.data.OfficerData
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.DutyChange

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class HomeActivityViewModelTest {

    @RelaxedMockK
    private lateinit var mockedRepository: Repository
    private lateinit var homeVM: HomeActivityViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testInitNotLoggedIn() {
        every { mockedRepository.isLoggedIn() } returns false

        homeVM =
            HomeActivityViewModel(mockedRepository, ApplicationProvider.getApplicationContext())
        assert(homeVM.userEventLiveData.value?.getContentIfNotHandled() == HomeActivityViewModel.HomeActivityUserEvent.AskUserLogoutEvent)
    }

    @Test
    fun testInitLoggedInOnDuty() {
        val mockedOfficer = mockkClass(OfficerData::class)
        every { mockedRepository.isLoggedIn() } returns true
        every { mockedRepository.getCurrentOfficer() } returns mockedOfficer
        every { mockedRepository.getRecentOnDutyChange() } returns DutyChange().apply {
            status = ON_DUTY
        }

        homeVM =
            HomeActivityViewModel(mockedRepository, ApplicationProvider.getApplicationContext())

        assert(homeVM.currentOfficerLiveData.value == mockedOfficer)

        assert(homeVM.onDutyStatusLiveData.value == true)
        assert(homeVM.onDutyTextStatusLiveData.value == context.getString(R.string.at_sea))
    }

    @Test
    fun testInitLoggedInOffDuty() {
        val mockedOfficer = mockkClass(OfficerData::class)
        every { mockedRepository.isLoggedIn() } returns true
        every { mockedRepository.getCurrentOfficer() } returns mockedOfficer
        every { mockedRepository.getRecentOnDutyChange() } returns DutyChange().apply {
            status = OFF_DUTY
        }

        homeVM =
            HomeActivityViewModel(mockedRepository, ApplicationProvider.getApplicationContext())

        assert(homeVM.currentOfficerLiveData.value == mockedOfficer)

        assert(homeVM.onDutyStatusLiveData.value == false)
        assert(homeVM.onDutyTextStatusLiveData.value == context.getString(R.string.not_at_sea))
    }

    @Test
    fun onDutyChanged() {
        val mockedOfficer = mockkClass(OfficerData::class)
        every { mockedRepository.isLoggedIn() } returns true
        every { mockedRepository.getCurrentOfficer() } returns mockedOfficer
        every { mockedRepository.getRecentOnDutyChange() } returns DutyChange().apply {
            status = OFF_DUTY
        }

        homeVM =
            HomeActivityViewModel(mockedRepository, ApplicationProvider.getApplicationContext())

        // Initial state = off duty
        assert(homeVM.onDutyStatusLiveData.value == false)
        assert(homeVM.onDutyTextStatusLiveData.value == context.getString(R.string.not_at_sea))

        // Dont save on duty if the same value
        homeVM.onDutyChanged(false)
        verify(exactly = 0) { mockedRepository.saveOnDutyChange(any(), any()) }

        homeVM.onDutyChanged(true)
        verify(exactly = 1) { mockedRepository.saveOnDutyChange(any(), any()) }

        assert(homeVM.onDutyStatusLiveData.value == true)
        assert(homeVM.onDutyTextStatusLiveData.value == context.getString(R.string.at_sea))

        // Don't save on duty if the same value
        homeVM.onDutyChanged(true)
        verify(exactly = 1) { mockedRepository.saveOnDutyChange(any(), any()) }
    }

    @Test
    fun logOutUser() {
        homeVM =
            HomeActivityViewModel(mockedRepository, ApplicationProvider.getApplicationContext())

        homeVM.logOutUser()

        assert(homeVM.userEventLiveData.value?.peekContent() == HomeActivityViewModel.HomeActivityUserEvent.AskUserLogoutEvent)
    }

    @Test
    fun logoutConfirmedSuccess() {
        every { mockedRepository.logOut(any(), any()) } answers {
            val successListener = firstArg() as () -> Unit
            successListener()
        }
        homeVM =
            HomeActivityViewModel(mockedRepository, ApplicationProvider.getApplicationContext())

        homeVM.logoutConfirmed()

        assert(homeVM.userEventLiveData.value?.peekContent() == HomeActivityViewModel.HomeActivityUserEvent.HomeUserLogoutEvent)
    }

    @Test
    fun logoutConfirmedFailed() {
        every { mockedRepository.logOut(any(), any()) } answers {
            val errorListener = secondArg() as (Throwable) -> Unit
            errorListener(RuntimeException("Failed to logout"))
        }
        homeVM =
            HomeActivityViewModel(mockedRepository, ApplicationProvider.getApplicationContext())

        homeVM.logoutConfirmed()
    }
}