package org.wildaid.ofish.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.item_photo.view.*
import org.wildaid.ofish.R
import org.wildaid.ofish.util.setVisible

class PhotosAdapter(
    private val dataList: MutableList<PhotoItem> = ArrayList(),
    var editMode: Boolean,
    private val photoRemoveListener: (item: PhotoItem) -> Unit
) : RecyclerView.Adapter<PhotosAdapter.EditPhotoViewHolder>() {

    fun setItems(items: List<PhotoItem>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun getItemCount() = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditPhotoViewHolder {
        return EditPhotoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_photo, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: EditPhotoViewHolder, position: Int) {
        holder.bindItem(dataList[position])
    }

    inner class EditPhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val radiusInPixels =
            view.context.resources.getDimensionPixelSize(R.dimen.photo_corner_radius)

        fun bindItem(item: PhotoItem) {
            Glide
                .with(itemView.context)
                .load(
                    item.localUri ?:
                    item.photo.pictureURL.ifBlank { null } ?:
                    item.photo.picture ?:
                    item.photo.thumbNail
                )

                .transforms(CenterCrop(), RoundedCorners(radiusInPixels))
                .placeholder(R.drawable.ic_image_placeholder)
                .into(itemView.item_edit_photo)

            itemView.item_edit_photo_remove.setVisible(editMode)

            itemView.item_edit_photo_remove.setOnClickListener {
                photoRemoveListener.invoke(item)
            }
        }
    }
}