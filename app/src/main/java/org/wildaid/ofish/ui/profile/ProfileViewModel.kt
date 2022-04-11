package org.wildaid.ofish.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.data.Repository


class ProfileViewModel(val repository: Repository) : ViewModel() {

    fun saveProfileImage(uri: Uri) {
        repository.updateCurrentOfficerPhoto(uri)
    }
}