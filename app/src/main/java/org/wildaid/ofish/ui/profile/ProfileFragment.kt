package org.wildaid.ofish.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_user_profile.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentUserProfileBinding
import org.wildaid.ofish.ui.base.ConfirmationDialogFragment
import org.wildaid.ofish.ui.base.DIALOG_CLICK_EVENT
import org.wildaid.ofish.ui.base.DialogButton
import org.wildaid.ofish.ui.base.DialogClickEvent
import org.wildaid.ofish.ui.home.ASK_CHANGE_DUTY_DIALOG_ID
import org.wildaid.ofish.ui.home.ASK_TO_LOGOUT_DIALOG_ID
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.util.getViewModelFactory

class ProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private val activityViewModel: HomeActivityViewModel by viewModels { getViewModelFactory() }
    private var dataBinding: FragmentUserProfileBinding? = null
    private val navigation: NavController by lazy { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI(view)
        setObservers()
        subscribeToDialogEvents()
        showOfficerPhoto()
//        dataBinding!!.switchDutyStatus.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                activityViewModel.onDutyChanged(isChecked)
//            } else {
//                showDutyReport()
//            }
//        }
    }

    private fun showOfficerPhoto() {
        image_user?.let {
            val photo = activityViewModel.repository.getCurrentOfficerPhoto()
            Glide.with(this)
                .load(photo?.getResourceForLoading())
                .placeholder(R.drawable.ic_account_circle)
                .into(it)
        }
    }

    private fun setObservers() {
        activityViewModel.userEventLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                HomeActivityViewModel.UserEvent.AskUserLogoutEvent -> askToLogout()
                HomeActivityViewModel.UserEvent.UserLogoutEvent -> onUserLoggedOut()
                HomeActivityViewModel.UserEvent.BecomeNotAtSea -> navigateToPatrolSummary()
                HomeActivityViewModel.UserEvent.AskDutyConfirmationEvent -> askToChangeDuty()
            }
        })
    }

    private fun navigateToPatrolSummary() {
        navigation.navigate(R.id.action_profileFragment_to_patrolSummaryFragment2)
    }

    private fun onUserLoggedOut() {
        navigation.navigate(R.id.action_profileFragment_to_login_activity)
        activity?.finish()
    }

    private fun askToChangeDuty() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ASK_CHANGE_DUTY_DIALOG_ID,
            getString(R.string.you_are_not_at_sea),
            getString(R.string.change_status_to_at_sea),
            getString(R.string.yes),
            getString(android.R.string.cancel)
        ).bundle()

        navigation.navigate(R.id.ask_change_duty_dialog, dialogBundle)
    }

    private fun askToLogout() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ASK_TO_LOGOUT_DIALOG_ID,
            getString(R.string.logout_dialog_title),
            getString(R.string.logout_dialog_message),
            getString(R.string.logout_dialog_yes),
            getString(android.R.string.cancel)
        ).bundle()

        navigation.navigate(R.id.action_profileFragment_to_ask_logout_dialog, dialogBundle)
    }

    private fun initUI(view: View) {
        dataBinding = FragmentUserProfileBinding.bind(view).apply {
            this.activityViewModel = this@ProfileFragment.activityViewModel
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
                if (handleDialogClick(click)) {
                    navStack.savedStateHandle.remove<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                }
            }
        })
    }

    private fun handleDialogClick(event: DialogClickEvent): Boolean {
        return when (event.dialogId) {
            ASK_TO_LOGOUT_DIALOG_ID -> {
                if (event.dialogBtn == DialogButton.POSITIVE) activityViewModel.logoutConfirmed()
                true
            }
            ASK_CHANGE_DUTY_DIALOG_ID -> {
                if (event.dialogBtn == DialogButton.POSITIVE) activityViewModel.onDutyChanged(true)
                true
            }
            else -> false
        }
    }
}