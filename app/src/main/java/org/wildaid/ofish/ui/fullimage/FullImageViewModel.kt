package org.wildaid.ofish.ui.fullimage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.ui.base.PhotoItem

class FullImageViewModel(val repository: Repository) : ViewModel() {

    private var _photoLiveData = MutableLiveData<PhotoItem>()
    val photoLiveData: LiveData<PhotoItem>
        get() = _photoLiveData

    /** Looks for photo in current report photos. If no, looks for in Realm */
    fun findPhoto(currentReportPhotos: MutableList<PhotoItem>, id: String) {
        val photoItem = currentReportPhotos.find { it.photo._id.toHexString() == id }
        if (photoItem != null) {
            _photoLiveData.value = photoItem
        } else {
            repository.getPhotoById(id)?.let {
                _photoLiveData.value = PhotoItem(it)
            }
        }
    }
}