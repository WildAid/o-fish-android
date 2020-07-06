package org.wildaid.ofish.ui.search.complex

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.wildaid.ofish.R
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.ViolationRisk
import org.wildaid.ofish.databinding.*
import org.wildaid.ofish.ui.search.base.BaseSearchAdapter
import org.wildaid.ofish.ui.search.base.BaseViewHolder
import org.wildaid.ofish.util.setVisible

const val BUSINESS_TYPE = 1
const val VIOLATION_TYPE = 2
const val RECORDS_TYPE = 3
const val CREW_TYPE = 4
const val REPORT_TYPE = 5
const val ADD_TYPE = 99
const val TEXTVIEW_TYPE = 101

class ComplexSearchAdapter(itemListener: (SearchModel) -> Unit) :
    BaseSearchAdapter<SearchModel>(itemListener) {
    @Suppress("UNCHECKED_CAST")
    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int)
            : BaseViewHolder<SearchModel> {
        return when (viewType) {
            BUSINESS_TYPE -> BusinessHolder(
                inflater.inflate(
                    R.layout.item_business_violation,
                    parent,
                    false
                )
            ) as BaseViewHolder<SearchModel>

            VIOLATION_TYPE -> ViolationHolder(
                inflater.inflate(
                    R.layout.item_business_violation,
                    parent,
                    false
                )
            ) as BaseViewHolder<SearchModel>

            RECORDS_TYPE -> RecordHolder(
                inflater.inflate(
                    R.layout.item_record,
                    parent,
                    false
                )
            ) as BaseViewHolder<SearchModel>

            CREW_TYPE -> CrewHolder(
                inflater.inflate(
                    R.layout.item_search_crew,
                    parent,
                    false
                )
            ) as BaseViewHolder<SearchModel>

            ADD_TYPE -> AddHolder(
                inflater.inflate(
                    R.layout.item_add,
                    parent,
                    false
                )
            ) as BaseViewHolder<SearchModel>

            TEXTVIEW_TYPE -> TextViewHolder(
                inflater.inflate(
                    R.layout.item_textview,
                    parent,
                    false
                )
            ) as BaseViewHolder<SearchModel>

            REPORT_TYPE -> ReportHolder(
                inflater.inflate(
                    R.layout.item_vessel_record,
                    parent,
                    false
                )
            ) as BaseViewHolder<SearchModel>

            else -> throw IllegalArgumentException("Unsupported holder type $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataList[position]) {
            is RecordSearchModel -> RECORDS_TYPE
            is ViolationSearchModel -> VIOLATION_TYPE
            is BusinessSearchModel -> BUSINESS_TYPE
            is CrewSearchModel -> CREW_TYPE
            is DutyReportSearchModel -> REPORT_TYPE
            is AddSearchModel -> ADD_TYPE
            is TextViewSearchModel -> TEXTVIEW_TYPE
            else -> super.getItemViewType(position)
        }
    }

    inner class ViolationHolder(view: View) : BaseViewHolder<ViolationSearchModel>(view) {
        private val binding: ItemBusinessViolationBinding = ItemBusinessViolationBinding.bind(view)
        private val viewArray = arrayOf(
            binding.itemBusinessAdress1,
            binding.itemBusinessAdress2,
            binding.itemBusinessCountry
        )

        override fun bindItem(item: ViolationSearchModel) {
            binding.itemBusinessName.text = item.value.code
            if (item.value.explanation.isBlank()) {
                viewArray.forEach {
                    it.setVisible(false)
                }
            } else {
                val dataArray = item.value.explanation.split(',', limit = viewArray.size)
                dataArray.forEachIndexed { index, data ->
                    viewArray[index].text = data
                    viewArray[index].setVisible(true)
                }

                for (i in dataArray.size until viewArray.size) {
                    viewArray[i].setVisible(false)
                }
            }
        }
    }

    inner class BusinessHolder(view: View) : BaseViewHolder<BusinessSearchModel>(view) {
        private val binding: ItemBusinessViolationBinding = ItemBusinessViolationBinding.bind(view)

        override fun bindItem(item: BusinessSearchModel) {
            binding.itemBusinessName.text = item.value.first
            val array = item.value.second.split(',', limit = 3)
            binding.itemBusinessAdress1.text = array[0]
            binding.itemBusinessAdress2.text = array[1]
            if (array.size > 2) binding.itemBusinessCountry.text = array[2]
        }
    }

    inner class RecordHolder(view: View) : BaseViewHolder<RecordSearchModel>(view) {
        private val binding: ItemRecordBinding = ItemRecordBinding.bind(view)
        private val radiusInPixels =
            view.context.resources.getDimensionPixelSize(R.dimen.photo_corner_radius)

        override fun bindItem(item: RecordSearchModel) {
            val lastReport = item.reports.first()
            val context = binding.root.context

            binding.recordVesselName.text = item.vessel.name
            binding.recordVesselPermitNumber.text =
                context.getString(R.string.records_permit_number, item.vessel.permitNumber)
            binding.recordVesselLastBoarding.text =
                context.getString(R.string.record_last_contact, lastReport.date)
            binding.recordVesselCrewSize.text =
                context.getString(
                    R.string.crew_member_size,
                    lastReport.crew.size + 1
                ) // Adding Captain

            val safetyLevel = lastReport.inspection?.summary?.safetyLevel?.level
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

        private fun bindImage(item: RecordSearchModel, context: Context) {
            item.reports.forEach { report ->
                report.vessel?.attachments?.photoIDs?.firstOrNull()?.let {
                    item.repository.getPhotoById(it).also { photo ->
                        Glide
                            .with(context)
                            .load(
                                photo?.pictureURL?.ifBlank { null } ?: photo?.picture
                                ?: photo?.thumbNail
                            )
                            .transform(CenterCrop(), RoundedCorners(radiusInPixels))
                            .placeholder(R.drawable.ic_vessel_placeholder_2)
                            .into(binding.recordVesselImage)
                        return
                    }
                }
            }
        }
    }

    inner class CrewHolder(view: View) : BaseViewHolder<CrewSearchModel>(view) {
        lateinit var item: CrewSearchModel
        private val binding: ItemSearchCrewBinding =
            ItemSearchCrewBinding.bind(view).apply { holder = this@CrewHolder }

        override fun bindItem(item: CrewSearchModel) {
            binding.holder = this
            this.item = item
            binding.itemCaptain.setVisible(item.isCaptain)
        }
    }

    inner class ReportHolder(view: View) : BaseViewHolder<DutyReportSearchModel>(view) {
        lateinit var item: DutyReportSearchModel
        private val dataBinding: ItemVesselRecordBinding =
            ItemVesselRecordBindingImpl.bind(view)

        override fun bindItem(item: DutyReportSearchModel) {
            dataBinding.vesselRecordDate.text =
                itemView.context.getString(R.string.report_date, item.report.date)

            dataBinding.vesselRecordMap.apply {
                onCreate(null)
                getMapAsync {
                    MapsInitializer.initialize(rootView.context.applicationContext);
                    it.uiSettings.isMapToolbarEnabled = false
                    val cords =
                        LatLng(
                            item.report.location?.latitude ?: 0.0,
                            item.report.location?.longitude ?: 0.0
                        )
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

            val warningsCount = item.report.inspection?.summary?.violations!!
                .count { it.disposition == ViolationRisk.Warning.name }

            val citationCount = item.report.inspection?.summary?.violations!!
                .count { it.disposition == ViolationRisk.Citation.name }

            dataBinding.vesselRecordViolations.text = when {
                citationCount <= 0 && warningsCount <= 0 -> itemView.context.getString(R.string.zero_violations)
                citationCount > 0 && warningsCount > 0 -> itemView.context.getString(
                    R.string.warnings_citation_count,
                    warningsCount,
                    citationCount
                )
                warningsCount > 0 -> itemView.context.getString(
                    R.string.warnings_count,
                    warningsCount
                )
                citationCount > 0 -> itemView.context.getString(
                    R.string.citation_count,
                    citationCount
                )
                else -> ""
            }
        }
    }

    inner class AddHolder(view: View) : BaseViewHolder<AddSearchModel>(view) {
        lateinit var item: AddSearchModel
        private val binding: ItemAddBinding =
            ItemAddBinding.bind(view).apply { holder = this@AddHolder }

        override fun bindItem(item: AddSearchModel) {
            binding.holder = this
            this.item = item
        }
    }

    inner class TextViewHolder(view: View) : BaseViewHolder<TextViewSearchModel>(view) {
        lateinit var item: TextViewSearchModel

        private val binding: ItemTextviewBinding =
            ItemTextviewBinding.bind(view).apply { holder = this@TextViewHolder }

        override fun bindItem(item: TextViewSearchModel) {
            binding.holder = this
            this.item = item
            binding.root.isEnabled = false
        }
    }
}