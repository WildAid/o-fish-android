package org.wildaid.ofish.ui.base

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.content.FileProvider.getUriForFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.wildaid.ofish.R
import org.wildaid.ofish.app.OFISH_PROVIDER_SUFFIX
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.crew.N_A
import java.io.File


const val CARDS_OFFSET_SIZE = 48

private const val ATTACHMENT_DIALOG_ID = 1231
private const val REQUEST_PICK_IMAGE = 10001
private const val TEMP_TAKE_IMAGE_PREFIX = "taken_image"
private const val TEMP_TAKE_IMAGE_SUFFIX = ".jpeg"

abstract class BaseReportFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    lateinit var onNextListener: OnNextClickedListener
    lateinit var currentReport: Report
    lateinit var currentReportPhotos: MutableList<PhotoItem>

    protected val navigation: NavController by lazy { findNavController() }

    private var pendingImageUri: Uri? = null
    private var pendingNoteSelection: (() -> Unit)? = null
    private var pendingPhotoSelection: ((Uri) -> Unit)? = null

    protected var isFieldCheckPassed = false
    protected var requiredFields: Array<TextInputLayout> = emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeForAttachmentDialogResult()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            if (pendingPhotoSelection != null && pendingImageUri != null) {
                pendingPhotoSelection?.invoke(data?.data ?: pendingImageUri!!)
                pendingPhotoSelection = null
            }
        }
    }

    fun isFormValid(): Boolean {
        val result = isAllRequiredFieldsNotEmpty()
        isFieldCheckPassed = false
        return result
    }

    protected fun askForAttachmentType(onNoteSelected: () -> Unit, onPhotoSelected: (Uri) -> Unit) {
        pendingNoteSelection = onNoteSelected
        pendingPhotoSelection = onPhotoSelected

        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ATTACHMENT_DIALOG_ID,
            getString(R.string.select_attachment_dialog_title),
            getString(R.string.select_attachment_dialog_message),
            getString(R.string.select_attachment_dialog_note),
            getString(R.string.select_attachment_dialog_photo)
        ).bundle()

        navigation.navigate(R.id.confirmation_dialog, dialogBundle)
    }

    private fun onNoteAttachmentSelected() {
        pendingNoteSelection?.invoke()
        pendingNoteSelection = null
    }

    private fun onPhotoAttachmentSelected() {
        if (pendingPhotoSelection != null) {
            pickImage(pendingPhotoSelection!!)
        }
    }

    fun pickImage(pendingPhotoSelection: (Uri) -> Unit) {
        this.pendingPhotoSelection = pendingPhotoSelection

        val pickImageIntent = createGalleryIntent()
        val takePhotoIntent = createCameraIntent()

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

    private fun combineIntents(list: MutableList<Intent>, intent: Intent) {
        val resolvedInfo = requireContext().packageManager.queryIntentActivities(intent, 0)
        for (info in resolvedInfo) {
            val packageName = info.activityInfo.packageName
            val targetedIntent = Intent(intent).apply {
                setPackage(packageName)
            }
            list.add(targetedIntent)
        }
    }

    private fun createGalleryIntent() = Intent().apply {
        type = "image/*";
        action = Intent.ACTION_GET_CONTENT;
    }

    private fun createCameraIntent(): Intent {
        val imageCachePath = File(requireContext().externalCacheDir, Environment.DIRECTORY_PICTURES)
        imageCachePath.mkdirs()

        val tempImageFile =
            File.createTempFile(TEMP_TAKE_IMAGE_PREFIX, TEMP_TAKE_IMAGE_SUFFIX, imageCachePath)

        pendingImageUri = getUriForFile(
            requireContext(),
            requireContext().packageName + OFISH_PROVIDER_SUFFIX,
            tempImageFile
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pendingImageUri);
    }

    private fun subscribeForAttachmentDialogResult() {
        val navStack = navigation.currentBackStackEntry!!
        navStack.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            if (navStack.savedStateHandle.contains(DIALOG_CLICK_EVENT)) {
                val click = navStack.savedStateHandle.get<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                if (click.dialogId != ATTACHMENT_DIALOG_ID) return@LifecycleEventObserver
                if (pendingPhotoSelection == null || pendingNoteSelection == null) return@LifecycleEventObserver

                navStack.savedStateHandle.remove<DialogClickEvent>(DIALOG_CLICK_EVENT)

                when (click.dialogBtn) {
                    DialogButton.POSITIVE -> onNoteAttachmentSelected()
                    DialogButton.NEGATIVE -> onPhotoAttachmentSelected()
                    else -> {/*  Nothing */
                    }
                }
            }
        })
    }

    protected fun isAllRequiredFieldsNotEmpty(): Boolean {
        var result = true
        requiredFields.forEach {
            val text = it.editText?.text
            if (it.visibility == View.VISIBLE && (text.isNullOrBlank() || text.toString() == N_A)) {
                result = false
                it.errorIconDrawable = resources.getDrawable(R.drawable.ic_error_outline, null)
            }
        }
        isFieldCheckPassed = true
        return result
    }

    protected fun showSnackbarWarning() {
        Snackbar.make(
            requireView().findViewById(R.id.snackbar_container) ?: requireView(),
            R.string.continue_with_empty_fields,
            Snackbar.LENGTH_LONG
        ).apply {
            animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
            setAction(R.string.continue_action) { onNextListener.onNextClicked() }
            setActionTextColor(resources.getColor(R.color.tabs_amber, null))
        }.show()
    }
}

interface OnNextClickedListener {
    fun onNextClicked(isSubmitted: Boolean = false)
}
