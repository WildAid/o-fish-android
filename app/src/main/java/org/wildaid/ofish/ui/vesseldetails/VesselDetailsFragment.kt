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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.fragment_vessel_details.*
import kotlinx.android.synthetic.main.item_photo.view.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.databinding.FragmentVesselDetailsBinding
import org.wildaid.ofish.ui.base.DIALOG_CLICK_EVENT
import org.wildaid.ofish.ui.base.DialogButton
import org.wildaid.ofish.ui.base.DialogClickEvent
import org.wildaid.ofish.ui.createreport.KEY_CREATE_REPORT_VESSEL_PERMIT_NUMBER
import org.wildaid.ofish.ui.home.ASK_CHANGE_DUTY_DIALOG_ID
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.ui.reportdetail.KEY_REPORT_ID
import org.wildaid.ofish.util.getViewModelFactory

const val KEY_VESSEL_PERMIT_NUMBER = "permit_number"

class VesselDetailsFragment : Fragment(R.layout.fragment_vessel_details) {
    private val fragmentViewModel: VesselDetailsViewModel by viewModels { getViewModelFactory() }
    private val activityViewModel: HomeActivityViewModel by activityViewModels { getViewModelFactory() }
    private val navigation by lazy { findNavController() }
    private val vesselPermitNumber: String by lazy {
        requireArguments().getString(
            KEY_VESSEL_PERMIT_NUMBER,
            "INVALID_ID"
        )
    }
    private lateinit var dataBinding: FragmentVesselDetailsBinding
    private lateinit var recordsAdapter: VesselRecordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.activityViewModel = activityViewModel
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(vessel_details_toolbar)
        vessel_details_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)
        recordsAdapter = VesselRecordsAdapter {
            val bundle = bundleOf(KEY_REPORT_ID to it.report._id)
            navigation.navigate(R.id.report_details_fragment, bundle)
        }

        dataBinding = FragmentVesselDetailsBinding.bind(view).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        vessel_reports_recycler.apply {
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
            adapter = recordsAdapter
        }

        fragmentViewModel.vesselItemLiveData.observe(viewLifecycleOwner, Observer {
            recordsAdapter.setItems(it.reports)
        })

        fragmentViewModel.vesselPhotoLiveData.observe(viewLifecycleOwner, Observer {
            updateVesselPhoto(it)
        })

        fragmentViewModel.boardVesselLiveData.observe(viewLifecycleOwner, EventObserver {
            val navigationArgs = bundleOf(KEY_CREATE_REPORT_VESSEL_PERMIT_NUMBER to vesselPermitNumber)
            navigation.navigate(
                R.id.action_vessel_details_fragment_to_create_report,
                navigationArgs
            )
        })

        fragmentViewModel.loadVessel(vesselPermitNumber)
        subscribeToDialogEvents()
    }

    private fun updateVesselPhoto(it: Photo) {
        Glide
            .with(this)
            .load(
                it.pictureURL.ifBlank { null } ?:
                it.picture ?:
                it.thumbNail
            )
            .placeholder(R.drawable.ic_vessel_profile)
            .into(vessel_image)
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

    private fun handleDialogClick(event: DialogClickEvent) {
        when {
            event.dialogId == ASK_CHANGE_DUTY_DIALOG_ID && event.dialogBtn == DialogButton.POSITIVE -> {
                activityViewModel.onDutyChanged(true)
            }
        }
    }
}