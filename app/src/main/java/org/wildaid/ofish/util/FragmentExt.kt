package org.wildaid.ofish.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_vessel.view.*
import org.wildaid.ofish.R
import org.wildaid.ofish.app.OFISH_PROVIDER_SUFFIX
import org.wildaid.ofish.app.ServiceLocator
import org.wildaid.ofish.ui.base.ViewModelFactory
import java.io.File


const val TEMP_TAKE_IMAGE_PREFIX = "taken_image"
const val TEMP_TAKE_IMAGE_SUFFIX = ".jpeg"

fun Fragment.getViewModelFactory() =
    ViewModelFactory(
        ServiceLocator.provideRepository(this.requireContext()),
        this.requireActivity().application,
        this
    )

fun Fragment.hideKeyboard() {
    val activity = this.requireActivity()
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Find the currently focused view, so we can grab the correct window token from it.
    var view: View? = activity.currentFocus
    // If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    } else {
        view.clearFocus()
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.combineIntents(list: MutableList<Intent>, intent: Intent) {
    val resolvedInfo = requireContext().packageManager.queryIntentActivities(intent, 0)
    for (info in resolvedInfo) {
        val packageName = info.activityInfo.packageName
        val targetedIntent = Intent(intent).apply {
            setPackage(packageName)
        }
        list.add(targetedIntent)
    }
}

fun Fragment.createGalleryIntent() = Intent().apply {
    type = "image/*"
    action = Intent.ACTION_GET_CONTENT
}

fun Fragment.createCameraIntent(pendingImageUri: Uri): Intent {
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    return cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pendingImageUri)
}

fun Fragment.createImageUri(): Uri {
    val imageCachePath = File(requireContext().externalCacheDir, Environment.DIRECTORY_PICTURES)
    imageCachePath.mkdirs()

    val tempImageFile =
        File.createTempFile(TEMP_TAKE_IMAGE_PREFIX, TEMP_TAKE_IMAGE_SUFFIX, imageCachePath)
    return FileProvider.getUriForFile(
        requireContext(),
        requireContext().packageName + OFISH_PROVIDER_SUFFIX,
        tempImageFile
    )
}

fun Fragment.showSnackMessage(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}