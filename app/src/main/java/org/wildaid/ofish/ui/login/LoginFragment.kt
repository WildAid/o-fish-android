package org.wildaid.ofish.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login.*
import org.wildaid.ofish.BuildConfig
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.util.getViewModelFactory

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val loginViewModel by viewModels<LoginViewModel> { getViewModelFactory() }
    private lateinit var navigation: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navigation = findNavController()
        loginViewModel.loginLiveData.observe(viewLifecycleOwner, Observer(::handleLoginResult))
        loginViewModel.progressLiveData.observe(viewLifecycleOwner, EventObserver(::handleProgress))

        if (BuildConfig.REALM_USER.isBlank() || BuildConfig.REALM_PASSWORD.isBlank()) {
            Log.i(
                "Login Setup", "You can specify properties realm_user and/or realm_password in realm.properties to pre fill credentials"
            )
        }
        ed_user.setText(BuildConfig.REALM_USER)
        ed_password.setText(BuildConfig.REALM_PASSWORD)

        btn_login.setOnClickListener {
            loginViewModel.login(ed_user.text.toString(), ed_password.text.toString())
        }
    }


    private fun handleLoginResult(loginResult: LoginViewModel.LoginResult) {
        when (loginResult) {
            is LoginViewModel.LoginResult.LoginSuccess -> {
                navigation.navigate(R.id.home_screen)
                requireActivity().finish()
            }
            is LoginViewModel.LoginResult.LoginError ->{
                Snackbar.make(requireView(), loginResult.errorMsg.orEmpty(), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun handleProgress(showProgress: Boolean) {
        if (showProgress) {
            navigation.navigate(R.id.progress_dialog)
        } else if (navigation.currentDestination?.id == R.id.progress_dialog) {
            navigation.popBackStack()
        }
    }
}