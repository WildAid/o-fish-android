package org.wildaid.ofish.ui.base

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.parcel.Parcelize
import org.wildaid.ofish.R

private const val EXTRA_DIALOG_ID = "extra_dialog_id"
private const val EXTRA_DIALOG_TITLE = "extra_dialog_title"
private const val EXTRA_DIALOG_MESSAGE = "extra_dialog_message"
private const val EXTRA_DIALOG_POSITIVE = "extra_dialog_positive"
private const val EXTRA_DIALOG_NEGATIVE = "extra_dialog_negative"
private const val EXTRA_DIALOG_NEUTRAL = "extra_dialog_neutral"
private const val EXTRA_HIGHLIGHT_NEUTRAL = "extra_highlight_neutral"
private const val EXTRA_DIALOG_ARGS = "extra_dialog_args"

const val DIALOG_CLICK_EVENT = "dialog_click_event"

class ConfirmationDialogFragment : DialogFragment() {
    private var title: String? = null
    private lateinit var message: String
    private lateinit var positive: String
    private var dialogId: Int = 0
    private var negative: String? = null
    private var highLightNeutral: Boolean = false
    private var neutral: String? = null
    private var args: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogId = arguments?.getInt(EXTRA_DIALOG_ID) ?: 0
        title = arguments?.getString(EXTRA_DIALOG_TITLE)
        message = arguments?.getString(EXTRA_DIALOG_MESSAGE)!!
        positive = arguments?.getString(EXTRA_DIALOG_POSITIVE) ?: getString(android.R.string.ok)
        negative = arguments?.getString(EXTRA_DIALOG_NEGATIVE)
        neutral = arguments?.getString(EXTRA_DIALOG_NEUTRAL)
        highLightNeutral = arguments?.getBoolean(EXTRA_HIGHLIGHT_NEUTRAL, false) ?: false
        args = arguments?.getBundle(EXTRA_DIALOG_ARGS)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.BaseAlertDialog)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton(positive) { _: DialogInterface?, _: Int ->
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                DIALOG_CLICK_EVENT,
                DialogClickEvent(dialogId, DialogButton.POSITIVE)
            )
        }

        if (negative != null) {
            builder.setNegativeButton(negative) { _: DialogInterface?, _: Int ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    DIALOG_CLICK_EVENT,
                    DialogClickEvent(dialogId, DialogButton.NEGATIVE)
                )
            }
        }

        if (neutral != null) {
            builder.setNeutralButton(neutral) { _: DialogInterface?, _: Int ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    DIALOG_CLICK_EVENT,
                    DialogClickEvent(dialogId, DialogButton.NEUTRAL)
                )
            }
        }
        val dialog: AlertDialog = builder.create()

        dialog.setOnShowListener {
            if (neutral != null && highLightNeutral) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(resources.getColor(R.color.red))
            }
        }
        return dialog
    }

    class Bundler(
        var dialogId: Int,
        var title: String?,
        var message: String?,
        var positive: String?,
        var negative: String? = null
    ) {
        var neutral: String? = null
        var extras: Bundle? = null
        var highLightNegative = false

        fun bundle(): Bundle {
            val bundle = Bundle()
            bundle.putInt(EXTRA_DIALOG_ID, dialogId)
            bundle.putString(EXTRA_DIALOG_TITLE, title)
            bundle.putString(EXTRA_DIALOG_MESSAGE, message)
            bundle.putString(EXTRA_DIALOG_POSITIVE, positive)
            bundle.putString(EXTRA_DIALOG_NEGATIVE, negative)
            bundle.putString(EXTRA_DIALOG_NEUTRAL, neutral)
            bundle.putBundle(EXTRA_DIALOG_ARGS, extras)
            bundle.putBoolean(EXTRA_HIGHLIGHT_NEUTRAL, highLightNegative)
            return bundle
        }
    }
}

@Parcelize
data class DialogClickEvent(
    val dialogId: Int,
    val dialogBtn: DialogButton
) : Parcelable

enum class DialogButton {
    NEGATIVE, NEUTRAL, POSITIVE
}