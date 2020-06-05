package org.wildaid.ofish.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.home.HomeActivity
import org.wildaid.ofish.ui.login.LoginActivity
import org.wildaid.ofish.util.getViewModelFactory

private const val SPLASH_DELAY = 3000L

class SplashActivity : AppCompatActivity(R.layout.activity_splash) {
    private val splashViewModel by viewModels<SplashViewModel> { getViewModelFactory() }
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashViewModel.checkUser()
    }

    override fun onStart() {
        super.onStart()
        splashViewModel.authLiveData.observe(this, Observer(::handleAuthState))
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    private fun handleAuthState(state: SplashViewModel.AuthState) {
        val intent = when (state) {
            SplashViewModel.AuthState.AuthRequired -> Intent(this, LoginActivity::class.java)
            SplashViewModel.AuthState.AuthComplete -> Intent(this, HomeActivity::class.java)
        }

        handler.postDelayed({
            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }
}