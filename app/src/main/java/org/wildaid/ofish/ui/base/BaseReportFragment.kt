package org.wildaid.ofish.ui.base

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.util.combineIntents
import org.wildaid.ofish.util.createCameraIntent
import org.wildaid.ofish.util.createGalleryIntent
import org.wildaid.ofish.util.createImageUri

const val CARDS_OFFSET_SIZE = 48
private const val ATTACHMENT_DIALOG_ID = 1231
const val REQUEST_PICK_IMAGE = 10001
const val PHOTO_ID = "photo_id"

abstract class BaseReportFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    lateinit var onNextListener: OnNextClickedListener
    lateinit var currentReport: Report
    lateinit var currentReportPhotos: MutableList<PhotoItem>

    protected val navigation: NavController by lazy { findNavController() }

    private var pendingImageUri: Uri? = null
    private var pendingNoteSelection: (() -> Unit)? = null
    private var pendingPhotoSelection: ((Uri) -> Unit)? = null

    protected var isFieldCheckPassed = false

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

    abstract fun isAllRequiredFieldsNotEmpty(): Boolean

    abstract fun validateForms(): Boolean

    protected fun validateField(field: TextInputLayout): Boolean {
        var result = true
        val text = field.editText?.text
        if (field.visibility == View.VISIBLE && text.isNullOrBlank()) {
            result = false
            field.errorIconDrawable = resources.getDrawable(R.drawable.ic_error_outline, null)
        }
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
            peekImage(pendingPhotoSelection!!)
        }
    }

    fun peekImage(pendingPhotoSelection: (Uri) -> Unit) {
        this.pendingPhotoSelection = pendingPhotoSelection

        val pickImageIntent = createGalleryIntent()
        pendingImageUri = createImageUri()
        val takePhotoIntent = createCameraIntent(pendingImageUri!!)

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

    protected fun showFullImage(view: View, photoItem: PhotoItem) {
        val bundle = bundleOf(PHOTO_ID to photoItem.photo._id.toHexString())
        val extra = FragmentNavigatorExtras(view to view.transitionName)
        navigation.navigate(R.id.action_tabsFragment_to_fullImageFragment, bundle, null, extra)
    }
}

interface OnNextClickedListener {
    fun onNextClicked(isSubmitted: Boolean = false)
}