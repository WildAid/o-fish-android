package org.wildaid.ofish.ui.login

import android.os.Build
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.realm.mongodb.AppException
import io.realm.mongodb.ErrorCode
import io.realm.mongodb.User
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
        val success = slot<(User) -> Unit>()

        every {
            mockedRepository.login("Username", any(), any(), any())
        } returns Unit

        assert(!(loginVM.progressLiveData.value?.peekContent()?: false))

        loginVM.login("Username", "Password")

        assert(loginVM.progressLiveData.value?.peekContent() == true)

        verify(exactly = 1) {
            mockedRepository.login(
                any(),
                any(),
                capture(success),
                any()
            )
        }

        success.captured.invoke(mockkClass(User::class))

        assert(loginVM.progressLiveData.value?.peekContent() == false)
    }

    @Test
    fun loginFailed() {
        val ex = AppException(ErrorCode.AUTH_ERROR, "AUTHENTICATION ERROR")
        every { mockedRepository.login(any(), any(), captureLambda(), captureLambda()) } answers {
            val errorLambda: (AppException) -> Unit = args[3] as (AppException) -> Unit
            errorLambda.invoke(ex)
        }

        loginVM.login("UserName", "Password")

        assert(loginVM.loginLiveData.value is LoginViewModel.LoginResult.LoginError)
    }

    @Test
    fun loginSuccess() {
        every { mockedRepository.login(any(), any(), any(), any()) } answers {
            val success: (User) -> Unit = thirdArg()
            success.invoke(mockkClass(User::class))
        }
        loginVM.login("UserName", "Password")
        assert(loginVM.loginLiveData.value == LoginViewModel.LoginResult.LoginSuccess)
    }
}