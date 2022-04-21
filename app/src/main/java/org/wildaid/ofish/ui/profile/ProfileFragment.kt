package org.wildaid.ofish.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.fragment_user_profile.image_user
import kotlinx.android.synthetic.main.fragment_user_profile.image_user_status
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentUserProfileBinding
import org.wildaid.ofish.ui.base.DIALOG_CLICK_EVENT
import org.wildaid.ofish.ui.base.DialogButton
import org.wildaid.ofish.ui.base.DialogClickEvent
import org.wildaid.ofish.ui.base.REQUEST_PICK_IMAGE
import org.wildaid.ofish.ui.home.ASK_CHANGE_DUTY_DIALOG_ID
import org.wildaid.ofish.ui.home.ASK_TO_LOGOUT_DIALOG_ID
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.ui.home.NOT_AT_SEA_DIALOG_ID
import org.wildaid.ofish.util.*

class ProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private val activityViewModel: HomeActivityViewModel by activityViewModels { getViewModelFactory() }
    private var dataBinding: FragmentUserProfileBinding? = null
    private val navigation: NavController by lazy { findNavController() }
    private val fragmentViewModel: ProfileViewModel by viewModels { getViewModelFactory() }
    private lateinit var pendingImageUri: Uri


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI(view)
        subscribeToDialogEvents()
        showOfficerPhoto()
        setObservers()
    }

    private fun setObservers() {
        activityViewModel.onDutyStatusLiveData.observe(viewLifecycleOwner, Observer { onDuty ->
            image_user_status.isEnabled = onDuty
        })

        activityViewModel.onDraftBoardsDeletedSuccessListener.observe(
            viewLifecycleOwner) { onSuccess -> if (onSuccess) activityViewModel.logoutConfirmed()
        }
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


    private fun initUI(view: View) {
        dataBinding = FragmentUserProfileBinding.bind(view).apply {
            this.lifecycleOwner = lifecycleOwner
            this.activityViewModel = this@ProfileFragment.activityViewModel
        }

        dataBinding?.let {
            it.imageUser.setOnClickListener { pickUserImage() }
            it.toolbarUserProfile.setNavigationOnClickListener { navigation.navigateUp() }
        }
    }

    private fun pickUserImage() {
        val pickImageIntent = createGalleryIntent()
        pendingImageUri = createImageUri()
        val takePhotoIntent = createCameraIntent(pendingImageUri)

        val chooser = Intent.createChooser(pickImageIntent, getString(R.string.chose_image_source))
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent))
        startActivityForResult(chooser, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: pendingImageUri
            fragmentViewModel.saveProfileImage(uri)
            updateProfilePhoto(uri)
        }
    }

    private fun updateProfilePhoto(uri: Uri) {
        Glide.with(this)
            .clear(image_user)

        Glide.get(requireContext()).clearMemory()
        Thread {
            Glide.get(requireContext()).clearDiskCache()
        }.start()

        loadOfficerPhotoWithUri(uri, image_user)
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
                if (event.dialogBtn == DialogButton.POSITIVE) {

                    activityViewModel.removeDraftedBoardsBeforeLogout()
                }
                true
            }
            ASK_CHANGE_DUTY_DIALOG_ID -> {
                if (event.dialogBtn == DialogButton.POSITIVE) {
                    activityViewModel.onDutyChanged(true)
                }

                if (event.dialogBtn == DialogButton.NEGATIVE) {
                    activityViewModel.onDutyChanged(false)
                }
                val onDuty = activityViewModel.onDutyStatusLiveData.value ?: false
                switch_duty_status.isChecked = onDuty
                image_user_status.isEnabled = onDuty
                true
            }
            NOT_AT_SEA_DIALOG_ID->{
                if (event.dialogBtn == DialogButton.POSITIVE){
                    goOffDuty()
                    activityViewModel.logOutUser()
                }
                true
            }
            else -> false
        }
    }

    private fun goOffDuty() {
        dataBinding?.switchDutyStatus?.isChecked=false
        activityViewModel.onDutyChanged(false)
    }
}