package org.wildaid.ofish.util

import android.app.Application
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel

fun AndroidViewModel.getString(@StringRes stringId: Int) =
    getApplication<Application>().resources.getString(stringId)

fun AndroidViewModel.getString(@StringRes stringId: Int, formatArgs: Any?) =
    getApplication<Application>().resources.getString(stringId, formatArgs)

fun AndroidViewModel.getStringArray(@ArrayRes stringId: Int): Array<String> =
    getApplication<Application>().resources.getStringArray(stringId)