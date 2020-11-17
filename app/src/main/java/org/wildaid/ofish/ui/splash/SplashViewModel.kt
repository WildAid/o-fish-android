package org.wildaid.ofish.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.data.Repository

class SplashViewModel(val repository: Repository) : ViewModel() {

    private val _authLiveData = MutableLiveData<AuthState>()
    val authLiveData: LiveData<AuthState>
        get() = _authLiveData

    fun checkUser() {
        _authLiveData.value = if (repository.restoreLoggedUser() == null) {
            AuthState.AuthRequired
        } else {
            AuthState.AuthComplete
        }
    }

    sealed class AuthState {
        object AuthRequired : AuthState()
        object AuthComplete : AuthState()
    }
}