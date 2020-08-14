package org.wildaid.ofish.ui.login

import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.data.Repository

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class LoginViewModelTest {

    private lateinit var loginVM: LoginViewModel

    @MockK
    private lateinit var mockedRepository: Repository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        loginVM = LoginViewModel(mockedRepository)
    }

    @Test
    fun testProgress() {

    }

    @Test
    fun loginSuccess() {
        // TODO this example. We do not need to test behaviour, only results
        val userNameCapture = slot<String>()
        val userPassCapture = slot<String>()
        loginVM.login("mock name", "mock pass")

        verify(exactly = 1) {
            mockedRepository.login(
                capture(userNameCapture),
                capture(userPassCapture),
                any(),
                any()
            )
        }
        assert(userNameCapture.captured == "mock name")
    }

    @Test
    fun loginFailed() {

    }
}