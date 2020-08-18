package org.wildaid.ofish.util

import androidx.appcompat.app.AppCompatActivity
import org.wildaid.ofish.app.ServiceLocator
import org.wildaid.ofish.ui.base.ViewModelFactory

fun AppCompatActivity.getViewModelFactory() =
     ViewModelFactory(ServiceLocator.provideRepository(this), application, this)