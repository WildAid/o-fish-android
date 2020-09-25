package org.wildaid.ofish.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.util.getString


class LoginViewModel(val repository: Repository, application: Application) :
    AndroidViewModel(application) {

    private var _loginLiveData = MutableLiveData<LoginResult>()
    val loginLiveData: LiveData<LoginResult>
        get() = _loginLiveData

    private var _progressLiveData = MutableLiveData<Event<Boolean>>()
    val progressLiveData: LiveData<Event<Boolean>>
        get() = _progressLiveData

    fun login(userName: String, password: String) {
        repository.login(userName, password,
            loginSuccess = {
                Log.d("Login", "Logged in as $it")
                _progressLiveData.value = Event(false)
                _loginLiveData.value = LoginResult.LoginSuccess
            },
            loginError = {
                Log.d("Login", "Cannot log in. Error ${it?.errorMessage}")
                _progressLiveData.value = Event(false)
                _loginLiveData.value = LoginResult.LoginError(getString(R.string.login_error))
            }
        )

        _progressLiveData.value = Event(true)
    }

    sealed class LoginResult {
        object LoginSuccess : LoginResult()
        class LoginError(val errorMsg: String?) : LoginResult()
    }
}