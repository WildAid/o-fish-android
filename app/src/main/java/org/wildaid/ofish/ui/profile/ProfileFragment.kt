package org.wildaid.ofish.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_user_profile.*
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentUserProfileBinding
import org.wildaid.ofish.ui.base.DIALOG_CLICK_EVENT
import org.wildaid.ofish.ui.base.DialogButton
import org.wildaid.ofish.ui.base.DialogClickEvent
import org.wildaid.ofish.ui.base.REQUEST_PICK_IMAGE
import org.wildaid.ofish.ui.home.ASK_CHANGE_DUTY_DIALOG_ID
import org.wildaid.ofish.ui.home.ASK_TO_LOGOUT_DIALOG_ID
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.util.*

class ProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private val activityViewModel: HomeActivityViewModel by activityViewModels { getViewModelFactory() }
    private var dataBinding: FragmentUserProfileBinding? = null
    private val navigation: NavController by lazy { findNavController() }
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

        dataBinding!!.imageUser.setOnClickListener { pickUserImage() }
    }

    private fun pickUserImage() {
        val pickImageIntent = createGalleryIntent()
        pendingImageUri = createImageUri()
        val takePhotoIntent = createCameraIntent(pendingImageUri)

        val intentList: MutableList<Intent> = mutableListOf()
        combineIntents(intentList, pickImageIntent)
        combineIntents(intentList, takePhotoIntent)

        val chooserIntent: Intent?
        if (intentList.size > 0) {
            chooserIntent = Intent.createChooser(
                intentList.removeAt(intentList.size - 1),
                getString(R.string.chose_image_source)
            )
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                intentList.toTypedArray()
            )

            startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE)
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
                if (event.dialogBtn == DialogButton.POSITIVE) {
                    activityViewModel.logoutConfirmed()
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
            else -> false
        }
    }
}