package org.wildaid.ofish.ui.vesseldetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.wildaid.ofish.R
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.databinding.ItemVesselRecordBinding
import org.wildaid.ofish.ui.base.AdapterDiffCallback


class VesselRecordsAdapter(
    val onItemClick: (ReportItem) -> Unit
) : RecyclerView.Adapter<VesselRecordsAdapter.RecordViewHolder>() {
    private val dataList: ArrayList<ReportItem> = ArrayList()

    fun setItems(items: List<ReportItem>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffCallback(items, dataList))
        diffResult.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(items)
    }

    override fun getItemCount() = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        return RecordViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_vessel_record,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bindItem(dataList[position])
    }

    inner class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dataBinding = ItemVesselRecordBinding.bind(view)

        fun bindItem(item: ReportItem) {
            dataBinding.root.setOnClickListener { onItemClick.invoke(item) }
            dataBinding.vesselRecordDate.text =
                itemView.context.getString(R.string.report_date, item.report.date)

            dataBinding.vesselRecordMap.apply {
                onCreate(null)
                getMapAsync {
                    MapsInitializer.initialize(rootView.context.applicationContext)
                    it.uiSettings.isMapToolbarEnabled = false
                    val cords = if (item.report.location.size == 2) {
                        LatLng(
                            item.report.location[0] ?: .0,
                            item.report.location[1] ?: .0
                        )
                    } else {
                        LatLng(.0, .0)
                    }
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(cords, 10f))
                    it.addMarker(MarkerOptions().position(cords))
                    it.mapType = GoogleMap.MAP_TYPE_NORMAL
                }
            }

            val safetyLevel = item.report.inspection?.summary?.safetyLevel?.level
            for (value in SafetyColor.values()) {
                if (value.name == safetyLevel) {
                    dataBinding.vesselSafetyLevel.setSafetyColor(
                        value,
                        R.dimen.safety_background_radius_small
                    )
                    break
                }
            }

            dataBinding.vesselRecordViolations.text = when {
                item.citationCount <= 0 && item.warningsCount <= 0 -> itemView.context.getString(R.string.zero_violations)
                item.citationCount > 0 && item.warningsCount > 0 -> itemView.context.getString(
                    R.string.warnings_citation_count,
                    item.warningsCount,
                    item.citationCount
                )
                item.warningsCount > 0 -> itemView.context.getString(
                    R.string.warnings_count,
                    item.warningsCount
                )
                item.citationCount > 0 -> itemView.context.getString(
                    R.string.citation_count,
                    item.citationCount
                )
                else -> ""
            }
        }
    }
}