package org.wildaid.ofish.ui.crew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.ItemCrewMemberBinding
import org.wildaid.ofish.ui.base.AdapterDiffCallback
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.setVisible


class CrewAdapter(
    private val dataList: ArrayList<CrewMemberItem> = ArrayList(),
    private val crewRemoveListener: (Int) -> Unit,
    private val crewEditModeListener: (CrewMemberItem) -> Unit,
    private val crewAddAttachmentListener: (CrewMemberItem) -> Unit,
    private val crewRemovePhotoListener: (PhotoItem, CrewMemberItem) -> Unit,
    private val crewRemoveNoteListener: (CrewMemberItem) -> Unit,
    private val crewChangeListener: () -> Unit

) : RecyclerView.Adapter<CrewAdapter.CrewMemberViewHolder>() {

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<CrewMemberItem>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun getItemCount() = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CrewMemberViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_crew_member,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: CrewMemberViewHolder, position: Int) {
        holder.bindItem(dataList[position])
    }

    override fun getItemId(position: Int): Long {
        return dataList[position].crewMember.hashCode().toLong()
    }

    inner class CrewMemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var currentItem: CrewMemberItem
        private val binding: ItemCrewMemberBinding =
            ItemCrewMemberBinding.bind(view).apply {
                holder = this@CrewMemberViewHolder
            }

        fun bindItem(item: CrewMemberItem) {
            // Need to trigger redrawing of item
            binding.holder = this
            currentItem = item

            binding.crewMemberEditGroup.setVisible(item.inEditMode)
            binding.crewMemberViewGroup.setVisible(!item.inEditMode)
            binding.crewMemberRemoveGroup.setVisible(item.isRemovable && item.inEditMode)
            binding.crewMemberEditNoteLayout.setVisible(item.inEditMode && item.attachments.hasNotes())
            binding.crewViewInfoLayout.crewViewAttachments.attachmentNoteGroup.setVisible(item.attachments.hasNotes())

            binding.crewEditPhotos.onPhotoRemoveListener = {
                crewRemovePhotoListener.invoke(it, item)
            }

            binding.crewMemberEditNoteLayout.setEndIconOnClickListener{
                crewRemoveNoteListener.invoke(item)
            }

            binding.crewMemberActionRemove.apply {
                text = context.getString(R.string.crew_member_remove_member, item.nameOrTitle())
            }
        }

        fun onNameChanged(name: CharSequence) {
            crewChangeListener.invoke()

            binding.crewMemberActionRemove.apply {
                post {
                    text = context.getString(
                        R.string.crew_member_remove_member,
                        currentItem.nameOrTitle()
                    )
                }
            }
        }

        fun getNameHint(): String {
            return if (currentItem.isCaptain)
                binding.root.resources.getString(R.string.captains_name)
            else binding.root.resources.getString(R.string.crew_member_name)
        }

        fun onLicenseChanged(license: CharSequence) {
            crewChangeListener.invoke()
        }

        fun onNoteChanged(note: CharSequence) {
            crewChangeListener.invoke()
        }

        fun onItemRemove() {
            crewRemoveListener.invoke(adapterPosition)
        }

        fun onEditClicked() {
            crewEditModeListener.invoke(currentItem)
        }

        fun onAttachmentClicked() {
            crewAddAttachmentListener.invoke(currentItem)
        }
    }
}