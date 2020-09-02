package org.wildaid.ofish.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.util.getString


class LoginViewModel(val repository: Repository, application: Application) :
    AndroidViewModel(application) {
    val loginLiveData = MutableLiveData<LoginResult>()
    val progressLiveData = MutableLiveData<Event<Boolean>>()

    fun login(userName: String, password: String) {
        repository.login(userName, password,
            loginSuccess = {
                Log.d("Login", "Logged in as $it")
                progressLiveData.value = Event(false)
                loginLiveData.value = LoginResult.LoginSuccess
            },
            loginError = {
                Log.d("Login", "Cannot log in. Error ${it?.errorMessage}")
                progressLiveData.value = Event(false)
                loginLiveData.value = LoginResult.LoginError(getString(R.string.login_error))
            }
        )

        progressLiveData.value = Event(true)
    }

    sealed class LoginResult {
        object LoginSuccess : LoginResult()
        class LoginError(val errorMsg: String?) : LoginResult()
    }
}