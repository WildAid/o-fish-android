package org.wildaid.ofish.ui.catches

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.*
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

class CatchViewModel(
    val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    val catchItemsLiveData = MutableLiveData<List<CatchItem>>()
    val buttonId = MutableLiveData<Event<Int>>()

    private val catchTitle = getString(R.string.catch_title)
    private val currentCatchItems = mutableListOf<CatchItem>()

    private lateinit var currentReport: Report
    private lateinit var currentReportPhotos: MutableList<PhotoItem>

    fun initCatch(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        this.currentReport = report
        this.currentReportPhotos = currentReportPhotos
        addCatch()
    }

    fun updateSpeciesForCatch(species: String, catchItem: CatchItem) {
        catchItem.catch.fish = species
        catchItemsLiveData.value = currentCatchItems
    }

    fun onNextClicked() {
        buttonId.value = Event(R.id.btn_next)
    }

    fun addCatch() {
        val newCatch = Catch()
        currentReport.inspection?.actualCatch?.add(newCatch)

        val newItem = CatchItem(
            catch = newCatch,
            title = "$catchTitle ${currentCatchItems.size + 1}",
            inEditMode = true,
            amount = "",
            attachmentItem = AttachmentItem(newCatch.attachments!!)
        )

        currentCatchItems.add(newItem)
        notifyCatchWithEdit(currentCatchItems, newItem)
    }

    fun removeCatch(position: Int) {
        currentCatchItems.removeAt(position)
        currentReport.inspection!!.actualCatch.removeAt(position)
        notifyCatchWithEdit(currentCatchItems)
    }

    fun editCatch(catchItem: CatchItem) {
        val catches = catchItemsLiveData.value.orEmpty().toMutableList()
        notifyCatchWithEdit(catches, catchItem)
        catchItemsLiveData.value = catches
    }

    fun addNoteForCatch(catchItem: CatchItem) {
        currentCatchItems.find {
            it.title == catchItem.title
        }?.attachmentItem?.addNote()

        catchItemsLiveData.value = currentCatchItems
    }

    fun removeNoteFromCatch(catchItem: CatchItem) {
        currentCatchItems.find {
            it.title == catchItem.title
        }?.attachmentItem?.removeNote()
        catchItemsLiveData.value = currentCatchItems
    }

    fun addPhotoForCatch(imageUri: Uri, catchItem: CatchItem) {
        val newPhotoItem = createPhoto(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentCatchItems.find {
            it.title == catchItem.title
        }?.attachmentItem?.addPhoto(newPhotoItem)
        catchItemsLiveData.value = currentCatchItems
    }

    fun removePhotoFromCatch(photo: PhotoItem, catchItem: CatchItem) {
        currentReportPhotos.remove(photo)
        currentCatchItems.find {
            it.title == catchItem.title
        }?.attachmentItem?.removePhoto(photo)
        catchItemsLiveData.value = currentCatchItems
    }

    private fun notifyCatchWithEdit(list: MutableList<CatchItem>, itemInEdit: CatchItem? = null) {
        catchItemsLiveData.value = list.also {
            it.forEachIndexed { index, item ->
                if (itemInEdit != null) {
                    item.inEditMode = item == itemInEdit
                }
                item.title = "$catchTitle ${index + 1}"
            }
        }
    }

    private fun createPhoto(imageUri: Uri): PhotoItem {
        return PhotoItem(
            Photo().apply {
                referencingReportID = currentReport._id.toString()
            },
            imageUri
        )
    }
}