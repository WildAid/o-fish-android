package org.wildaid.ofish.ui.catches

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.realm.RealmList
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Catch
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.BaseReportViewModel
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

class CatchViewModel(
    repository: Repository,
    app: Application
) : BaseReportViewModel(repository, app) {

    private val _catchItemsLiveData = MutableLiveData<List<CatchItem>>()
    val catchItemsLiveData: LiveData<List<CatchItem>>
        get() = _catchItemsLiveData

    private val _catchUserEventLiveData = MutableLiveData<Event<CatchUserEvent>>()
    val catchUserEventLiveData: LiveData<Event<CatchUserEvent>>
        get() = _catchUserEventLiveData

    private val catchTitle = getString(R.string.catch_title)
    private val currentCatchItems = mutableListOf<CatchItem>()

    override fun initViewModel(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        super.initViewModel(report, currentReportPhotos)

        val catch = (currentReport.inspection?.actualCatch ?: RealmList()).ifEmpty {
            RealmList(Catch())
        }
        currentReport.inspection?.actualCatch = catch

        catch.forEachIndexed{index, it ->
            currentCatchItems.add(
                CatchItem(
                    catch = it,
                    title = "$catchTitle ${index.inc()}",
                    inEditMode = true,
                    attachmentItem = AttachmentItem(
                        it.attachments!!,
                        getPhotoItemsForIds(it.attachments!!.photoIDs)
                    )
                )
            )
        }
        _catchItemsLiveData.postValue(currentCatchItems)
    }

    fun updateSpeciesForCatch(species: String, catchItem: CatchItem) {
        catchItem.catch.fish = species
        _catchItemsLiveData.value = currentCatchItems
    }

    fun onNextClicked() {
        _catchUserEventLiveData.value = Event(CatchUserEvent.Next)
    }

    fun addCatch() {
        val newCatch = Catch()
        currentReport.inspection?.actualCatch?.add(newCatch)

        val newItem = CatchItem(
            catch = newCatch,
            title = "$catchTitle ${currentCatchItems.size + 1}",
            inEditMode = true,
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
        val catches = _catchItemsLiveData.value.orEmpty().toMutableList()
        notifyCatchWithEdit(catches, catchItem)
        _catchItemsLiveData.value = catches
    }

    fun addNoteForCatch(catchItem: CatchItem) {
        currentCatchItems.find {
            it.title == catchItem.title
        }?.attachmentItem?.addNote()

        _catchItemsLiveData.value = currentCatchItems
    }

    fun removeNoteFromCatch(catchItem: CatchItem) {
        currentCatchItems.find {
            it.title == catchItem.title
        }?.attachmentItem?.removeNote()
        _catchItemsLiveData.value = currentCatchItems
    }

    fun addPhotoForCatch(imageUri: Uri, catchItem: CatchItem) {
        val newPhotoItem = createPhotoItem(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentCatchItems.find {
            it.title == catchItem.title
        }?.attachmentItem?.addPhoto(newPhotoItem)
        _catchItemsLiveData.value = currentCatchItems
    }

    fun removePhotoFromCatch(photo: PhotoItem, catchItem: CatchItem) {
        currentReportPhotos.remove(photo)
        currentCatchItems.find {
            it.title == catchItem.title
        }?.attachmentItem?.removePhoto(photo)
        _catchItemsLiveData.value = currentCatchItems
    }

    private fun notifyCatchWithEdit(list: MutableList<CatchItem>, itemInEdit: CatchItem? = null) {
        _catchItemsLiveData.value = list.also {
            it.forEachIndexed { index, item ->
                if (itemInEdit != null) {
                    item.inEditMode = item == itemInEdit
                }
                item.title = "$catchTitle ${index + 1}"
            }
        }
    }

    sealed class CatchUserEvent {
        object Next : CatchUserEvent()
    }
}