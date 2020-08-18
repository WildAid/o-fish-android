package org.wildaid.ofish.ui.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.ItemEditReportNoteBinding
import org.wildaid.ofish.ui.base.AdapterDiffCallback
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.setVisible

class NotesAdapter(
    private val dataList: ArrayList<NoteItem> = ArrayList(),
    private val noteRemoveListener: (Int) -> Unit,
    private val editNoteListener: (item: NoteItem) -> Unit,
    private val addPhotoAttachmentListener: (item: NoteItem) -> Unit,
    private val noteOnPhotoClickListener: (View, PhotoItem) -> Unit,
    private val removePhotoAttachmentListener: (photo: PhotoItem, item: NoteItem) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<NoteItem>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun getItemCount() = dataList.size

    override fun getItemId(position: Int): Long {
        return dataList[position].note.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_edit_report_note,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindItem(dataList[position])
    }

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var currentItem: NoteItem

        private val binding = ItemEditReportNoteBinding.bind(view).apply {
            holder = this@NoteViewHolder
        }

        fun bindItem(item: NoteItem) {
            binding.holder = this
            currentItem = item

            binding.groupNoteEdit.setVisible(item.inEditMode)
            binding.groupNoteView.setVisible(!item.inEditMode)
            binding.reportNoteEditPhotos.onPhotoClickListener = noteOnPhotoClickListener::invoke
            binding.viewNoteLayout.attachmentsPhotos.onPhotoClickListener =
                noteOnPhotoClickListener::invoke
            binding.reportNoteEditPhotos.onPhotoRemoveListener = {
                removePhotoAttachmentListener.invoke(it, currentItem)
            }
        }

        fun onItemRemove() {
            noteRemoveListener.invoke(adapterPosition)
        }

        fun onAddPhotoAttachment() {
            addPhotoAttachmentListener.invoke(currentItem)
        }

        fun onEditNote() {
            editNoteListener.invoke(currentItem)
        }
    }
}