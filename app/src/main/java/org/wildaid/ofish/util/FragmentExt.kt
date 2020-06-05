package org.wildaid.ofish.util

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import org.wildaid.ofish.app.OFishApplication
import org.wildaid.ofish.ui.base.ViewModelFactory


fun Fragment.getViewModelFactory(): ViewModelFactory {
    val application = requireContext().applicationContext as OFishApplication
    val repository = application.repository
    return ViewModelFactory(repository, application, this)
}

fun Fragment.hideKeyboard() {
    val activity = this.requireActivity()
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view: View? = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    } else {
        view.clearFocus()
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}