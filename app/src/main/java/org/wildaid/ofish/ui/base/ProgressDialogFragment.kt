package org.wildaid.ofish.ui.base

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.wildaid.ofish.R

private const val MESSAGE = "message"

class ProgressDialogFragment : DialogFragment() {
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        message = arguments?.getString(MESSAGE) ?: getString(R.string.processing)
        retainInstance = true
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ProgressDialog(activity).apply {
            isIndeterminate = true
            setMessage(message)
        }
    }

    override fun onDestroyView() {
        val dialog = dialog
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }
}