package org.wildaid.ofish.util

import androidx.appcompat.app.AppCompatActivity
import org.wildaid.ofish.app.OFishApplication
import org.wildaid.ofish.ui.base.ViewModelFactory

fun AppCompatActivity.getViewModelFactory(): ViewModelFactory {
    val application = this.applicationContext as OFishApplication
    val repository = application.repository
    return ViewModelFactory(repository, application, this)
}