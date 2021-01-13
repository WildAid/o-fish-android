package org.wildaid.ofish.ui.notes

import org.wildaid.ofish.data.report.AnnotatedNote
import org.wildaid.ofish.ui.base.PhotoItem

data class NoteItem(
    var note: AnnotatedNote,
    var title: String,
    var inEditMode: Boolean,
    val photos: MutableList<PhotoItem> = mutableListOf()
) {
    fun hasPhotos() = photos.isNotEmpty()

    fun addPhoto(newPhoto: PhotoItem) {
        photos.add(newPhoto)
        note.photoIDs.add(newPhoto.photo._id.toString())
    }

    fun removePhoto(removed: PhotoItem) {
        photos.remove(removed)
        note.photoIDs.remove(removed.photo._id.toString())
    }
}