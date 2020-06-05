package org.wildaid.ofish.data

interface OnSaveListener {
    fun onSuccess()
    fun onError(it: Throwable)
}