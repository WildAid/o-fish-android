package org.wildaid.ofish.ui.vessel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.ItemEditVesselEmsBinding
import org.wildaid.ofish.ui.base.AdapterDiffCallback
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.setVisible

class EMSAdapter(
    private val dataList: ArrayList<EMSItem> = ArrayList(),
    private val fieldFocusListener: View.OnFocusChangeListener,
    private val emsEditModeListener: (EMSItem) -> Unit,
    private val editEmsTypeListener: (EMSItem) -> Unit,
    private val emsAddAttachmentListener: (EMSItem) -> Unit,
    private val emsRemoveListener: (Int) -> Unit,
    private val emsRemoveNoteListener: (EMSItem) -> Unit,
    private val emsOnPhotoClickListener: (View, PhotoItem) -> Unit,
    private val emsRemovePhotoListener: (PhotoItem, EMSItem) -> Unit
) : RecyclerView.Adapter<EMSAdapter.EMSHolder>() {

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<EMSItem>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun getItemId(position: Int): Long {
        return dataList[position].ems.hashCode().toLong()
    }

    override fun getItemCount() = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EMSHolder {
        return EMSHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_edit_vessel_ems, parent, false)
        )
    }


    override fun onBindViewHolder(holder: EMSHolder, position: Int) {
        holder.bindItem(dataList[position])
    }

    inner class EMSHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val stringOther = view.resources.getString(R.string.other)
        val binding = ItemEditVesselEmsBinding.bind(view)
        lateinit var currentItem: EMSItem

        fun bindItem(emsItem: EMSItem) {
            this.currentItem = emsItem
            binding.emsItem = emsItem
            binding.currentHolder = this

            binding.vesselEditEmsNoteLayout.setVisible(emsItem.attachments.hasNotes() && emsItem.inEditMode)
            binding.emsEditGroup.setVisible(emsItem.inEditMode)
            binding.emsViewGroup.setVisible(!emsItem.inEditMode)
            binding.emsViewLayout.emsItemAttachments.attachmentNoteGroup.setVisible(emsItem.attachments.hasNotes())
            binding.vesselEditEmsDescriptionLayout.setVisible(
                emsItem.inEditMode && emsItem.ems.emsType.equals(stringOther, true)
            )

            binding.emsItemEditPhotos.onPhotoClickListener = { view, item ->
                emsOnPhotoClickListener.invoke(view, item)
            }
            binding.emsViewLayout.emsItemAttachments.attachmentsPhotos.onPhotoClickListener =
                { view, item ->
                    emsOnPhotoClickListener.invoke(view, item)
                }
            binding.emsItemEditPhotos.onPhotoRemoveListener = {
                emsRemovePhotoListener.invoke(it, currentItem)
            }
        }

        fun onEmsEditMode(emsItem: EMSItem) {
            emsEditModeListener.invoke(emsItem)
        }

        fun onEmsAddAttachment(emsItem: EMSItem) {
            emsAddAttachmentListener.invoke(emsItem)
        }

        fun onEmsRemove(emsItem: EMSItem) {
            emsRemoveListener.invoke(adapterPosition)
        }

        fun onEmsRemoveNote(emsItem: EMSItem) {
            emsRemoveNoteListener.invoke(emsItem)
        }

        fun onEmsChooseType(emsItem: EMSItem) {
            editEmsTypeListener.invoke(emsItem)
        }
    }
}

