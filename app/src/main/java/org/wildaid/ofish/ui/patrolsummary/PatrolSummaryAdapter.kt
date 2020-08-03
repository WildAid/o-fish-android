package org.wildaid.ofish.ui.patrolsummary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.databinding.ItemRecordBinding
import org.wildaid.ofish.ui.base.AdapterDiffCallback

class PatrolSummaryAdapter(
    private val repository: Repository,
    private val itemListener: (Report) -> Unit
) : RecyclerView.Adapter<PatrolSummaryAdapter.PatrolSummaryViewHolder>() {

    private val dataList: ArrayList<Report> = ArrayList()

    init {
        setHasStableIds(true)
    }

    fun setItems(items: List<Report>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun getItemCount() = dataList.size

    override fun getItemId(position: Int): Long {
        return dataList[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PatrolSummaryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_record,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PatrolSummaryViewHolder, position: Int) {
        val item = dataList[position]
        holder.bindItem(item)
        holder.itemView.setOnClickListener { itemListener.invoke(item) }
    }

    inner class PatrolSummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ItemRecordBinding.bind(view)
        private val radiusInPixels =
            view.context.resources.getDimensionPixelSize(R.dimen.photo_corner_radius)

        fun bindItem(item: Report) {
            val context = binding.root.context

            binding.recordVesselName.text = item.vessel?.name
            binding.recordVesselPermitNumber.text =
                context.getString(R.string.records_permit_number, item.vessel?.permitNumber)
            binding.recordVesselLastBoarding.text =
                context.getString(R.string.record_last_contact, item.date)
            val crewSize = getCrewSize(item)
            binding.recordVesselCrewSize.text =
                context.resources.getQuantityString(
                    R.plurals.crew_member_size,
                    crewSize, crewSize
                )

            val safetyLevel = item.inspection?.summary?.safetyLevel?.level
            for (value in SafetyColor.values()) {
                if (value.name == safetyLevel) {
                    binding.recordVesselSafetyLevel.setSafetyColor(
                        value,
                        R.dimen.safety_background_radius_small
                    )
                    break
                }
            }

            bindImage(item, context)
        }

        private fun getCrewSize(lastReport: Report): Int {
            val captain = lastReport.captain
            val captainCount =
                if (captain?.name.isNullOrBlank() && captain?.license.isNullOrBlank()) 0 else 1
            return lastReport.crew.size + captainCount
        }

        private fun bindImage(report: Report, context: Context) {
            report.vessel?.attachments?.photoIDs?.firstOrNull()?.let {
                repository.getPhotoById(it).also { photo ->
                    Glide
                        .with(context)
                        .load(photo?.getResourceForLoading())
                        .transform(CenterCrop(), RoundedCorners(radiusInPixels))
                        .placeholder(R.drawable.ic_vessel_placeholder_2)
                        .into(binding.recordVesselImage)
                }
            }
        }
    }
}