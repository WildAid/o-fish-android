package org.wildaid.ofish.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.data.Repository

class SplashViewModel(val repository: Repository) : ViewModel() {
    val authLiveData = MutableLiveData<AuthState>()

    fun checkUser() {
        authLiveData.value = if (repository.restoreLoggedUser() == null) {
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