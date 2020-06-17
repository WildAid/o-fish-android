package org.wildaid.ofish.ui.base

import android.net.Uri
import org.wildaid.ofish.data.report.Attachments
import org.wildaid.ofish.data.report.Photo

data class AttachmentItem(
    var attachment: Attachments,
    var photos: MutableList<PhotoItem> = mutableListOf()
) {
    fun hasNotes(): Boolean = attachment.notes.isNotEmpty()
    fun hasPhotos(): Boolean = photos.isNotEmpty()

    fun addPhoto(newPhoto: PhotoItem) {
        photos.add(newPhoto)
        attachment.photoIDs.add(newPhoto.photo._id.toString())
    }

    fun removePhoto(removed: PhotoItem) {
        photos.remove(removed)
        attachment.photoIDs.remove(removed.photo._id.toString())
    }

    fun setNote(note: String?) {
        if (attachment.notes.isEmpty()) {
            attachment.notes.add(0, note)
        } else {
            attachment.notes[0] = note
        }
    }

    fun getNote(): String? {
        return attachment.notes.getOrNull(0)
    }

    fun addNote() {
        if (attachment.notes.isEmpty()) {
            attachment.notes.add(0, "")
        } else {
            attachment.notes[0] = ""
        }
    }

    fun removeNote() {
        attachment.notes.clear()
    }
}

data class PhotoItem(
    val photo: Photo,
    var localUri: Uri? = null
)