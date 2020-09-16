package org.wildaid.ofish.ui.vesseldetails

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_vessel_details.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.databinding.FragmentVesselDetailsBinding
import org.wildaid.ofish.ui.base.DIALOG_CLICK_EVENT
import org.wildaid.ofish.ui.base.DialogButton
import org.wildaid.ofish.ui.base.DialogClickEvent
import org.wildaid.ofish.ui.base.ItemDivider
import org.wildaid.ofish.ui.createreport.*
import org.wildaid.ofish.ui.home.ASK_CHANGE_DUTY_DIALOG_ID
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.ui.reportdetail.KEY_REPORT_ID
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.setVisible

const val KEY_VESSEL_PERMIT_NUMBER = "permit_number"
const val KEY_VESSEL_NAME = "vessel_name"

class VesselDetailsFragment : Fragment(R.layout.fragment_vessel_details) {
    private val fragmentViewModel: VesselDetailsViewModel by viewModels { getViewModelFactory() }
    private val activityViewModel: HomeActivityViewModel by activityViewModels { getViewModelFactory() }
    private val navigation by lazy { findNavController() }

    private val vesselPermitNumber by lazy {
        requireArguments().getString(
            KEY_VESSEL_PERMIT_NUMBER,
            "INVALID_ID"
        )
    }

    private val vesselName: String by lazy {
        requireArguments().getString(
            KEY_VESSEL_NAME,
            "INVALID_NAME"
        )
    }
    private lateinit var dataBinding: FragmentVesselDetailsBinding
    private lateinit var recordsAdapter: VesselRecordsAdapter
    private lateinit var photosAdapter: VesselPhotosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.activityViewModel = activityViewModel
        setHasOptionsMenu(true)

        recordsAdapter = VesselRecordsAdapter {
            val bundle = bundleOf(KEY_REPORT_ID to it.report._id)
            navigation.navigate(R.id.report_details_fragment, bundle)
        }

        photosAdapter = VesselPhotosAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(vessel_details_toolbar)
        vessel_details_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)

        dataBinding = FragmentVesselDetailsBinding.bind(view).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        vessel_image_pager.adapter = photosAdapter

        vessel_reports_recycler.apply {
            addItemDecoration(ItemDivider(requireContext(), LinearLayoutManager.VERTICAL))
            adapter = recordsAdapter
        }

        fragmentViewModel.vesselItemLiveData.observe(viewLifecycleOwner, Observer {
            recordsAdapter.setItems(it.reports)
        })

        fragmentViewModel.vesselPhotosLiveData.observe(viewLifecycleOwner, Observer {
            updateVesselImages(it)
        })

        fragmentViewModel.userEventLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is VesselDetailsUserEvent.AskOnDutyToNavigate -> navigateToCreateReport(it.report)
            }
        })

        fragmentViewModel.loadVessel(vesselPermitNumber, vesselName)
        subscribeToDialogEvents()
    }

    override fun onStart() {
        super.onStart()

        vessel_details_appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val scrollRange = vessel_details_appbar?.totalScrollRange!!
            if (scrollRange + verticalOffset == 0) {
                collapsing_toolbar.title = getString(R.string.back)
            } else if (verticalOffset == 0) {
                collapsing_toolbar.title = vesselName
            }
        })
    }

    private fun navigateToCreateReport(it: Report) {
        val prefillVessel = it.vessel!!.let { vessel ->
            PrefillVessel(
                vessel.name,
                vessel.permitNumber,
                vessel.nationality,
                vessel.homePort,
                vessel.attachments?.photoIDs?.toList() ?: emptyList()
            )
        }
        val prefillCrew = PrefillCrew(
            PrefillCrewMember(
                it.captain?.name!!,
                it.captain?.license!!,
                it.captain?.attachments?.photoIDs?.toList() ?: emptyList()
            ),
            it.crew.map { crewMember ->
                PrefillCrewMember(
                    crewMember.name,
                    crewMember.license,
                    crewMember.attachments?.photoIDs?.toList() ?: emptyList()
                )
            }
        )
        val navigationArgs =
            bundleOf(KEY_CREATE_REPORT_ARGS to CreateReportBundle(prefillVessel, prefillCrew))
        navigation.navigate(
            R.id.action_vessel_details_fragment_to_create_report,
            navigationArgs
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_report_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> navigation.popBackStack()
            R.id.menu_board_vessel -> fragmentViewModel.boardVessel()
            R.id.menu_search_records -> navigation.popBackStack()
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

    private fun updateVesselImages(photos: List<Photo>) {
        photosAdapter.setItems(photos)

        TabLayoutMediator(vessel_image_pager_indicator, vessel_image_pager) { _, _ ->
            // Empty
        }.attach()

        vessel_image_pager_indicator.setVisible(photos.size > 1)
    }

    private fun handleDialogClick(event: DialogClickEvent) {
        when {
            event.dialogId == ASK_CHANGE_DUTY_DIALOG_ID && event.dialogBtn == DialogButton.POSITIVE -> {
                activityViewModel.onDutyChanged(true)
                fragmentViewModel.boardVessel()
            }
        }
    }
}