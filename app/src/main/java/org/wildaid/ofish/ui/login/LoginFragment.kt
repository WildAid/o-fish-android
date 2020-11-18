package org.wildaid.ofish.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import org.wildaid.ofish.BuildConfig
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentLoginBinding
import org.wildaid.ofish.ui.base.ConfirmationDialogFragment
import org.wildaid.ofish.ui.base.DIALOG_CLICK_EVENT
import org.wildaid.ofish.ui.base.DialogButton
import org.wildaid.ofish.ui.base.DialogClickEvent
import org.wildaid.ofish.util.getViewModelFactory

const val LOGIN_ERROR_DIALOG = 401

class LoginFragment : Fragment() {

    private val loginViewModel by viewModels<LoginViewModel> { getViewModelFactory() }
    private val navigation: NavController by lazy { findNavController() }

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentLoginBinding.inflate(inflater, container, false)
                .also { this.binding = it }
                .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loginViewModel.loginLiveData.observe(viewLifecycleOwner, Observer(::handleLoginResult))
        loginViewModel.progressLiveData.observe(viewLifecycleOwner, EventObserver(::handleProgress))

        subscribeToDialogEvents()

        if (BuildConfig.REALM_USER.isBlank() || BuildConfig.REALM_PASSWORD.isBlank()) {
            Log.i(
                    "Login Setup",
                    "You can specify properties realm_user and/or realm_password in realm.properties to pre fill credentials"
            )
        }
        binding.edUser.setText(BuildConfig.REALM_USER)
        binding.edPassword.setText(BuildConfig.REALM_PASSWORD)

        binding.btnLogin.isEnabled = isUserFieldsValid()

        binding.edUser.addTextChangedListener(watcher)
        binding.edPassword.addTextChangedListener(watcher)

        binding.btnLogin.setOnClickListener {
            loginViewModel.login(binding.edUser.text.toString(), binding.edPassword.text.toString())
        }
    }

    private fun isUserFieldsValid() =
            !(binding.edUser.text.isNullOrBlank() || binding.edPassword.text.isNullOrBlank())

    private fun handleLoginResult(loginResult: LoginViewModel.LoginResult) {
        when (loginResult) {
            is LoginViewModel.LoginResult.LoginSuccess -> {
                navigation.navigate(R.id.home_screen)
                requireActivity().finish()
            }
            is LoginViewModel.LoginResult.LoginError -> {
                showLoginErrorDialog()
            }
        }
    }

    private fun subscribeToDialogEvents() {
        val navStack = navigation.currentBackStackEntry!!
        navStack.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            if (navStack.savedStateHandle.contains(DIALOG_CLICK_EVENT)) {
                val click = navStack.savedStateHandle.get<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                val handled = handleDialogClick(click)
                if (handled) {
                    navStack.savedStateHandle.remove<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                }
            }
        })
    }

    private fun handleDialogClick(click: DialogClickEvent): Boolean {
        return when (click.dialogId) {
            LOGIN_ERROR_DIALOG -> {
                if (click.dialogBtn == DialogButton.POSITIVE) {
                    navigation.popBackStack()
                }
                true
            }
            else -> false
        }
    }

    private fun showLoginErrorDialog() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
                LOGIN_ERROR_DIALOG,
                getString(R.string.login_error),
                getString(R.string.invalid_email_or_password),
                getString(android.R.string.ok)
        ).bundle()
        navigation.navigate(R.id.error_login_dialog, dialogBundle)
    }

    private fun handleProgress(showProgress: Boolean) {
        if (showProgress) {
            navigation.navigate(R.id.progress_dialog)
        } else if (navigation.currentDestination?.id == R.id.progress_dialog) {
            navigation.popBackStack()
        }
    }

    private val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.btnLogin.isEnabled = isUserFieldsValid()
        }
    }
}