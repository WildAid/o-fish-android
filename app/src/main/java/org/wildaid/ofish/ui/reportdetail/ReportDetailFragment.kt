package org.wildaid.ofish.ui.reportdetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_report_details.*
import org.bson.types.ObjectId
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.report.*
import org.wildaid.ofish.databinding.*
import org.wildaid.ofish.ui.base.*
import org.wildaid.ofish.ui.createreport.CreateReportBundle
import org.wildaid.ofish.ui.createreport.KEY_CREATE_REPORT_ARGS
import org.wildaid.ofish.ui.createreport.PrefillCrew
import org.wildaid.ofish.ui.createreport.PrefillVessel
import org.wildaid.ofish.ui.home.ASK_CHANGE_DUTY_DIALOG_ID
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.ui.home.ZOOM_LEVEL
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.setVisible

const val KEY_REPORT_ID = "report_id"
const val BOARD_VESSEL_ALLOWED = "should_show_button"

@SuppressLint("MissingPermission")
class ReportDetailFragment : Fragment(R.layout.fragment_report_details) {
    private val fragmentViewModel: ReportDetailViewModel by viewModels { getViewModelFactory() }
    private val activityViewModel: HomeActivityViewModel by activityViewModels { getViewModelFactory() }
    private val navigation by lazy { findNavController() }
    private val shouldShowButton by lazy {
        requireArguments().getBoolean(
            BOARD_VESSEL_ALLOWED,
            true
        )
    }
    private lateinit var fragmentBinding: FragmentReportDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val reportId = requireArguments().getSerializable(KEY_REPORT_ID) as ObjectId
        fragmentViewModel.loadReport(reportId)
        fragmentViewModel.activityViewModel = activityViewModel
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(report_toolbar)
        fragmentBinding = FragmentReportDetailsBinding.bind(view).apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.viewModel = fragmentViewModel
        }

        collectPhotoAttachments()

        fragmentViewModel.reportLiveData.observe(viewLifecycleOwner, Observer(::displayReport))

        fragmentViewModel.boardVesselLiveData.observe(viewLifecycleOwner, EventObserver {
            navigateToCreateReport(it)
        })

        (childFragmentManager.findFragmentById(R.id.report_map) as NestedScrollMapFragment?)?.attachParentScroll(
            report_scroll_view
        )

        report_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)
        subscribeToDialogEvents()

        boardVesselButton.visibility = if (shouldShowButton) View.VISIBLE else View.GONE
    }

    private fun navigateToCreateReport(it: Report) {
        val prefillCrew = PrefillCrew(
            Pair(it.captain?.name!!, it.captain?.license!!),
            it.crew.map { Pair(it.name, it.license) })
        val prefillVessel = PrefillVessel(
            it.vessel?.name!!,
            it.vessel?.permitNumber!!,
            it.vessel?.nationality!!,
            it.vessel?.homePort!!
        )
        val navigationArgs =
            bundleOf(KEY_CREATE_REPORT_ARGS to CreateReportBundle(prefillVessel, prefillCrew))

        navigation.navigate(
            R.id.action_report_details_fragment_to_create_report,
            navigationArgs
        )
    }

    private fun collectPhotoAttachments() {
        listOf(
            fragmentBinding.reportVesselViewInfo.vesselViewAttachments.attachmentsPhotos,
            fragmentBinding.reportViewLastDelivery.deliveryViewAttachments.attachmentsPhotos,
            fragmentBinding.reportCaptainView.crewViewAttachments.attachmentsPhotos
        ).forEach {
            it.onPhotoClickListener = ::showFullImage
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_report_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> navigation.popBackStack()
            R.id.menu_board_vessel -> fragmentViewModel.boardVessel()
            R.id.menu_search_records -> navigation.popBackStack(R.id.complex_search, false)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun subscribeToDialogEvents() {
        val navStack = navigation.currentBackStackEntry!!
        navStack.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            if (navStack.savedStateHandle.contains(DIALOG_CLICK_EVENT)) {
                val click = navStack.savedStateHandle.remove<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                handleDialogClick(click)
            }
        })
    }

    private fun handleDialogClick(event: DialogClickEvent) {
        when {
            event.dialogId == ASK_CHANGE_DUTY_DIALOG_ID && event.dialogBtn == DialogButton.POSITIVE -> {
                activityViewModel.onDutyChanged(true)
                fragmentViewModel.boardVessel()
            }
        }
    }

    private fun displayReport(report: Report) {
        report.vessel?.let {
            inflateVessel(it)
            inflateLastDelivery(it.lastDelivery!!)
            inflateEMS(it.ems)
        }

        report.captain?.let {
            inflateCrew(it, report.crew)
        }
        report.inspection?.also {
            inflateActivity(it)
            inflateCatch(it.actualCatch)

            it.summary?.violations?.also { violation ->
                inflateViolation(violation)
            }

            it.summary?.safetyLevel?.also { level ->
                inflateRiskLevel(level)
            }
        }

        inflateNotes(report.notes)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.report_map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            val coordinates = if (report.location.size == 2)
                LatLng(report.location[1] ?: .0, report.location[0] ?: .0)
            else LatLng(.0, .0)
            map.isMyLocationEnabled = true
            map.addMarker(MarkerOptions().position(coordinates))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, ZOOM_LEVEL))
        }
    }

    private fun inflateVessel(vessel: Boat) {
        fragmentBinding.reportVesselViewInfo.apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.photos = fragmentViewModel.getPhotosForIds(vessel.attachments?.photoIDs)
            this.note = vessel.attachments?.notes?.firstOrNull()
            this.vesselViewAttachments.attachmentNoteGroup.setVisible(
                vessel.attachments?.notes?.isNotEmpty() ?: false
            )
            this.vessel = vessel
        }
    }

    private fun inflateLastDelivery(lastDelivery: Delivery) {
        fragmentBinding.reportViewLastDelivery.apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.photos = fragmentViewModel.getPhotosForIds(lastDelivery.attachments?.photoIDs)
            this.note = lastDelivery.attachments?.notes?.firstOrNull()
            this.deliveryViewAttachments.attachmentNoteGroup.setVisible(
                lastDelivery.attachments?.notes?.isNotEmpty() ?: false
            )
            this.delivery = lastDelivery
        }
    }

    private fun inflateEMS(emsList: List<EMS>) {
        emsList.forEachIndexed { index, item ->
            val emsBinding = ItemReportEmsBinding.inflate(layoutInflater).apply {
                lifecycleOwner = viewLifecycleOwner
                ems = item
                photos = fragmentViewModel.getPhotosForIds(item.attachments?.photoIDs)
                note = item.attachments?.notes?.firstOrNull()
                emsItemAttachments.attachmentNoteGroup.setVisible(
                    item.attachments?.notes?.isNotEmpty() ?: false
                )
                reportEmsDivider.setVisible(index != emsList.size - 1)
                this.emsItemAttachments.attachmentsPhotos.onPhotoClickListener = ::showFullImage
            }
            fragmentBinding.reportEmsContainer.addView(emsBinding.root)
        }
    }

    private fun inflateCrew(captain: CrewMember, crew: List<CrewMember>) {
        fragmentBinding.reportCaptainView.apply {
            crewMember = captain
            photos = fragmentViewModel.getPhotosForIds(captain.attachments?.photoIDs)
            note = captain.attachments?.notes?.firstOrNull()
            crewViewAttachments.attachmentNoteGroup.setVisible(
                captain.attachments?.notes?.isNotEmpty() ?: false
            )
            nameHint = getString(R.string.captains_name)
            reportCrewDivider.setVisible(false)
        }

        crew.also {
            report_crew_title.text = getString(R.string.report_crew_count, it.size)
            it.forEachIndexed { index, item ->
                val crewBinding = ItemReportCrewBinding.inflate(layoutInflater).apply {
                    crewMember = item
                    photos = fragmentViewModel.getPhotosForIds(item.attachments?.photoIDs)
                    nameHint = getString(R.string.crew_member_name)
                    note = item.attachments?.notes?.firstOrNull()
                    crewViewAttachments.attachmentNoteGroup.setVisible(
                        item.attachments?.notes?.isNotEmpty() ?: false
                    )
                    reportCrewDivider.setVisible(index != it.size - 1)
                    this.crewViewAttachments.attachmentsPhotos.onPhotoClickListener =
                        ::showFullImage
                }
                report_crew_container.addView(crewBinding.root)
            }
        }
    }

    private fun inflateActivity(inspection: Inspection) {
        val activityBinding = ItemReportActivityBinding.inflate(layoutInflater).apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.inspection = inspection
            this.activityAttachments.photos =
                fragmentViewModel.getPhotosForIds(inspection.activity?.attachments?.photoIDs)
            this.gearAttachments.photos =
                fragmentViewModel.getPhotosForIds(inspection.gearType?.attachments?.photoIDs)
            this.fisheryAttachments.photos =
                fragmentViewModel.getPhotosForIds(inspection.fishery?.attachments?.photoIDs)

            this.activityAttachments.note = inspection.activity?.attachments?.notes?.firstOrNull()
            this.gearAttachments.note = inspection.gearType?.attachments?.notes?.firstOrNull()
            this.fisheryAttachments.note = inspection.fishery?.attachments?.notes?.firstOrNull()

            this.activityAttachments.attachmentNoteGroup.setVisible(
                inspection.activity?.attachments?.notes?.isNotEmpty() ?: false
            )
            this.gearAttachments.attachmentNoteGroup.setVisible(
                inspection.gearType?.attachments?.notes?.isNotEmpty() ?: false
            )
            this.fisheryAttachments.attachmentNoteGroup.setVisible(
                inspection.fishery?.attachments?.notes?.isNotEmpty() ?: false
            )
            this.activityAttachments.attachmentsPhotos.onPhotoClickListener = ::showFullImage
            this.gearAttachments.attachmentsPhotos.onPhotoClickListener = ::showFullImage
            this.fisheryAttachments.attachmentsPhotos.onPhotoClickListener = ::showFullImage
        }

        report_activity_container.addView(activityBinding.root)
    }

    private fun inflateCatch(catch: List<Catch>) {
        report_catch_title.text = getString(R.string.report_catch_count, catch.size)

        catch.forEach { item ->
            val catchBinding = ItemReportCatchBinding.inflate(layoutInflater)
            catchBinding.reportSpecies.text = item.fish
            catchBinding.photos = fragmentViewModel.getPhotosForIds(item.attachments?.photoIDs)
            catchBinding.note = item.attachments?.notes?.firstOrNull()
            catchBinding.catchViewAttachments.attachmentNoteGroup.setVisible(
                item.attachments?.notes?.isNotEmpty() ?: false
            )

            if (item.unit.isBlank() || item.weight <= 0) {
                catchBinding.reportCatchAmountType1.text = getString(R.string.count)
                catchBinding.reportCatchAmount1.text = item.number.toString()
            } else {
                catchBinding.reportCatchAmountType1.text = getString(R.string.weight)
                catchBinding.reportCatchAmount1.text = "${item.weight} ${item.unit}"

                if (item.number > 0) {
                    catchBinding.reportCatchAmountType2.text = getString(R.string.count)
                    catchBinding.reportCatchAmount2.text = item.number.toString()
                } else {
                    catchBinding.reportCatchAmountType2.setVisible(false)
                    catchBinding.reportCatchAmount2.setVisible(false)
                }
            }

            catchBinding.catchViewAttachments.attachmentsPhotos.onPhotoClickListener =
                ::showFullImage

            report_catch_container.addView(catchBinding.root)
        }
    }

    private fun inflateViolation(violations: List<Violation>) {
        report_violation_title.text = getString(R.string.report_violations_count, violations.size)

        violations.forEachIndexed { index, item ->
            val violationBinding = ItemReportViolationBinding.inflate(layoutInflater)
            violationBinding.violation = item
            violationBinding.reportViolationDivider.setVisible(index != violations.size - 1)
            violationBinding.photos = fragmentViewModel.getPhotosForIds(item.attachments?.photoIDs)
            violationBinding.note = item.attachments?.notes?.firstOrNull()
            violationBinding.violationAttachments.attachmentNoteGroup.setVisible(
                item.attachments?.notes?.isNotEmpty() ?: false
            )
            violationBinding.violationAttachments.attachmentsPhotos.onPhotoClickListener =
                ::showFullImage
            report_violation_container.addView(violationBinding.root)
        }
    }

    private fun inflateNotes(notes: List<AnnotatedNote>) {
        notes.forEachIndexed { index, note ->
            val noteBinding = ItemViewAttachmentBinding.inflate(layoutInflater)
            noteBinding.note = note.note
            noteBinding.photos = fragmentViewModel.getPhotosForIds(note.photoIDs)
            noteBinding.noteTitle = getString(R.string.report_note_indexed, index + 1)
            noteBinding.reportNotesDivider.setVisible(index != notes.lastIndex)
            noteBinding.attachmentsPhotos.onPhotoClickListener = ::showFullImage
            report_notes_container.addView(noteBinding.root)
        }
    }

    private fun inflateRiskLevel(safetyLevel: SafetyLevel) {
        for (value in SafetyColor.values()) {
            if (value.name == safetyLevel.level) {
                report_color_status.setSafetyColor(value, R.dimen.safety_background_radius_big)
                break
            }
        }
    }

    private fun showFullImage(view: View, photoItem: PhotoItem) {
        val bundle = bundleOf(PHOTO_ID to photoItem.photo._id.toHexString())
        val extra = FragmentNavigatorExtras(view to view.transitionName)
        navigation.navigate(
            R.id.action_report_details_fragment_to_fullImageFragment,
            bundle,
            null,
            extra
        )
    }
}