package org.wildaid.ofish.ui.violation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.databinding.ItemEditViolationBinding
import org.wildaid.ofish.ui.base.AdapterDiffCallback
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.setVisible


class ViolationAdapter(
    private val dataList: ArrayList<ViolationItem> = ArrayList(),
    private val captain: CrewMember?,
    private val violationSearchListener: (buttonId: Int, item: ViolationItem) -> Unit,
    private val violationEditModeListener: (item: ViolationItem) -> Unit,
    private val violationAttachmentListener: (item: ViolationItem) -> Unit,
    private val violationRemoveListener: (Int) -> Unit,
    private val violationRemoveNoteListener: (item: ViolationItem) -> Unit,
    private val violationOnPhotoClickListener: (View, PhotoItem) -> Unit,
    private val violationRemovePhotoListener: (PhotoItem, item: ViolationItem) -> Unit
) : RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder>() {

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<ViolationItem>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun getItemCount() = dataList.size

    override fun getItemId(position: Int): Long {
        return dataList[position].violation.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViolationViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_edit_violation,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int) {
        holder.bindItem(dataList[position])
    }

    inner class ViolationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var currentItem: ViolationItem
        private val stringOther = view.resources.getString(R.string.other)

        private val binding: ItemEditViolationBinding =
            ItemEditViolationBinding.bind(view).apply {
                holder = this@ViolationViewHolder
            }

        fun bindItem(item: ViolationItem) {
            binding.holder = this
            currentItem = item
            binding.violationActionRemove.text =
                binding.root.context.getString(R.string.remove_violation, item.title)

            binding.violationWarning.isSelected =
                isWarningCitationSelected(binding.violationWarning.text)
            binding.violationCitation.isSelected =
                isWarningCitationSelected(binding.violationCitation.text)

            binding.violationViewGroup.setVisible(!item.inEditMode)
            binding.violationEditGroup.setVisible(item.inEditMode)
            binding.violationViewLayout.violationAttachments.attachmentNoteGroup
                .setVisible(item.attachments.hasNotes())

            binding.violationNoteLayout.setEndIconOnClickListener {
                violationRemoveNoteListener.invoke(item)
            }

            binding.violationEditPhotos.onPhotoClickListener = violationOnPhotoClickListener::invoke
            binding.violationViewLayout.violationAttachments.attachmentsPhotos.onPhotoClickListener =
                violationOnPhotoClickListener::invoke
            binding.violationEditPhotos.onPhotoRemoveListener = {
                violationRemovePhotoListener.invoke(it, item)
            }

            binding.violationNoteLayout.setEndIconOnClickListener {
                violationRemoveNoteListener.invoke(item)
            }

            binding.violationNoteLayout.setVisible(item.inEditMode && item.attachments.hasNotes())
            binding.violationRemoveGroup.setVisible(item.inEditMode && dataList.size > 1)

            binding.violationDescriptionLayout.setVisible(
                item.inEditMode && item.violation.offence?.code.equals(
                    stringOther, true
                )
            )
            binding.itemCaptain.setVisible(captain != null && currentItem.violation.crewMember == captain)
        }

        private fun isWarningCitationSelected(text: CharSequence): Boolean {
            return currentItem.violation.disposition.equals(text.toString(), true)
        }

        fun onResultClicked(selectedButton: Int) {
            val buttons = listOf(binding.violationCitation, binding.violationWarning)
            buttons.forEach {
                if (it.id == selectedButton) {
                    currentItem.violation.disposition = it.text as String
                    it.isSelected = true
                } else {
                    it.isSelected = false
                }
            }
        }

        fun onItemRemove() {
            violationRemoveListener.invoke(adapterPosition)
        }

        fun onViolationEditClicked() {
            violationEditModeListener.invoke(currentItem)
        }

        fun onViolationAddAttachmentClicked() {
            violationAttachmentListener.invoke(currentItem)
        }

        fun onSearchClicked(id: Int) {
            violationSearchListener.invoke(id, currentItem)
        }
    }
}