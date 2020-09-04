package org.wildaid.ofish.ui.notes

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.AnnotatedNote
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

class NotesViewModel(
    val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    private var _notesLiveData = MutableLiveData<List<NoteItem>>()
    val notesLiveData: LiveData<List<NoteItem>>
        get() = _notesLiveData

    private var _buttonId = MutableLiveData<Event<Int>>()
    val buttonId: LiveData<Event<Int>>
        get() = _buttonId

    private val noteTitle = getString(R.string.note)

    private lateinit var currentReport: Report
    private lateinit var currentReportPhotos: MutableList<PhotoItem>

    fun initNotes(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        this.currentReport = report
        this.currentReportPhotos = currentReportPhotos
        addNote()
    }

    fun addNote() {
        val noteItems = notesLiveData.value.orEmpty().toMutableList()
        val createdNote = AnnotatedNote()

        currentReport.notes.add(createdNote)
        noteItems.add(NoteItem(createdNote, "$noteTitle ${noteItems.size + 1}", true))
        notifyNotesWithEditItem(noteItems, noteItems.lastOrNull())
    }

    fun removeNote(position: Int) {
        val noteItems = notesLiveData.value.orEmpty().toMutableList()
        noteItems.removeAt(position)
        currentReport.notes.removeAt(position)
        notifyNotesWithEditItem(noteItems)
    }

    fun editNote(editingItem: NoteItem) {
        val noteItems = notesLiveData.value.orEmpty().toMutableList()
        notifyNotesWithEditItem(noteItems, editingItem)
    }

    fun addPhotoAttachmentForNote(uri: Uri, noteItem: NoteItem) {
        val newPhotoItem = createPhoto(uri)
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
        _buttonId.value = Event(R.id.btn_save)
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

    private fun createPhoto(imageUri: Uri): PhotoItem {
        return PhotoItem(
            Photo().apply {
                referencingReportID = currentReport._id.toString()
            },
            imageUri
        )
    }
}