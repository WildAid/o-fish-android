package org.wildaid.ofish.ui.notes

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.realm.RealmList
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.AnnotatedNote
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.BaseReportViewModel
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

class NotesViewModel(
    repository: Repository,
    app: Application
) : BaseReportViewModel(repository, app) {

    private var _notesLiveData = MutableLiveData<List<NoteItem>>()
    val notesLiveData: LiveData<List<NoteItem>>
        get() = _notesLiveData

    private var _notesUserEventLiveData = MutableLiveData<Event<NotesUserEvent>>()
    val notesUserEventLiveData: LiveData<Event<NotesUserEvent>>
        get() = _notesUserEventLiveData

    private val noteTitle = getString(R.string.note)
    private var currentNoteItems = mutableListOf<NoteItem>()

    override fun initViewModel(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        super.initViewModel(report, currentReportPhotos)

        val notes = report.notes.ifEmpty { RealmList(AnnotatedNote()) }
        notes.forEachIndexed { index, it ->
            currentNoteItems.add(
                NoteItem(
                    note = it,
                    title = "$noteTitle ${index.inc()}",
                    inEditMode = true,
                    photos = getPhotoItemsForIds(it.photoIDs)
                )
            )
        }

        _notesLiveData.postValue(currentNoteItems)
    }

    fun addNote() {
        val createdNote = AnnotatedNote()
        currentReport.notes.add(createdNote)
        currentNoteItems.add(NoteItem(createdNote, "$noteTitle ${currentNoteItems.size + 1}", true))
        notifyNotesWithEditItem(currentNoteItems, currentNoteItems.lastOrNull())
    }

    fun removeNote(position: Int) {
        currentNoteItems.removeAt(position)
        currentReport.notes.removeAt(position)
        notifyNotesWithEditItem(currentNoteItems)
    }

    fun editNote(editingItem: NoteItem) {
        notifyNotesWithEditItem(currentNoteItems, editingItem)
    }

    fun addPhotoAttachmentForNote(uri: Uri, noteItem: NoteItem) {
        val newPhotoItem = createPhotoItem(uri)
        currentReportPhotos.add(newPhotoItem)
        noteItem.addPhoto(newPhotoItem)
        _notesLiveData.value = notesLiveData.value
    }

    fun removePhotoAttachment(photo: PhotoItem, note: NoteItem) {
        currentReportPhotos.remove(photo)
        note.removePhoto(photo)
        _notesLiveData.value = notesLiveData.value
    }

    fun onNextClicked() {
        _notesUserEventLiveData.value = Event(NotesUserEvent.SaveEvent)
    }

    private fun notifyNotesWithEditItem(items: List<NoteItem>, editingNote: NoteItem? = null) {
        _notesLiveData.value = items.also {
            it.forEachIndexed { index, item ->
                if (editingNote != null) {
                    item.inEditMode = item == editingNote
                }
                item.title = "$noteTitle ${index + 1}"
            }
        }
    }

    sealed class NotesUserEvent {
        object SaveEvent : NotesUserEvent()
    }
}