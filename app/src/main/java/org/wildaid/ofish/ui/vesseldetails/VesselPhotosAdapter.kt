package org.wildaid.ofish.ui.vesseldetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_vessel_image.view.*
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.ui.base.AdapterDiffCallback

class VesselPhotosAdapter : RecyclerView.Adapter<VesselPhotosAdapter.VesselPhotoHolder>() {
    private val dataList: ArrayList<Photo> = arrayListOf(Photo())

    fun setItems(items: List<Photo>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun getItemCount() = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VesselPhotoHolder {
        return VesselPhotoHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_vessel_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VesselPhotoHolder, position: Int) {
        holder.bindPhoto(dataList[position])
    }

    class VesselPhotoHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindPhoto(photo: Photo) {
            Glide
                .with(itemView.context)
                .load(
                    photo.pictureURL.ifBlank { null } ?: photo.picture ?: photo.thumbNail
                )
                .placeholder(R.drawable.ic_vessel_profile)
                .into(itemView.vessel_image)
        }
    }
}