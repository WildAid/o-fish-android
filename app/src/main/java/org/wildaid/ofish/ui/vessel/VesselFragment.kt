package org.wildaid.ofish.ui.vessel

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_vessel.*
import kotlinx.android.synthetic.main.fragment_vessel.view.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.Boat
import org.wildaid.ofish.databinding.FragmentVesselBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.complex.BusinessSearchModel
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment
import org.wildaid.ofish.ui.search.simple.SimpleSearchFragment
import org.wildaid.ofish.util.*
import java.util.*


const val CREATE_NEW_BUSINESS = "create_new_business"

class VesselFragment : BaseReportFragment(R.layout.fragment_vessel) {
    private val fragmentViewModel by viewModels<VesselViewModel> { getViewModelFactory() }
    private var pendingEmsForType: EMSItem? = null
    private lateinit var fragmentBinding: FragmentVesselBinding
    private lateinit var emsAdapter: EMSAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initVessel(currentReport, currentReportPhotos)
        subscribeToSearchResult()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentBinding = FragmentVesselBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = fragmentViewModel
        }

        requiredFields = arrayOf(
            vessel_name_layout, vessel_permit_number_layout, vessel_home_port_layout,
            vessel_flag_state_layout, delivery_business_layout, delivery_location_layout
        )

        emsAdapter = EMSAdapter(
            fieldFocusListener = fragmentViewModel.fieldFocusListener,
            emsEditModeListener = fragmentViewModel::editEms,
            emsRemoveListener = {
                hideKeyboard()
                fragmentViewModel.removeEms(it)
            },
            emsRemoveNoteListener = fragmentViewModel::removeNoteFromEms,
            editEmsTypeListener = {
                pendingEmsForType = it
                val searchBundle =
                    bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to SimpleSearchFragment.SearchEms)
                navigation.navigate(R.id.action_tabsFragment_to_simple_search, searchBundle)
            },
            emsAddAttachmentListener = { emsItem ->
                askForAttachmentType(
                    onNoteSelected = { fragmentViewModel.addNoteForEms(emsItem) },
                    onPhotoSelected = { fragmentViewModel.addPhotoForEms(it, emsItem) }
                )
            },
            emsOnPhotoClickListener = ::showFullImage,
            emsRemovePhotoListener = { photoItem, emsItem ->
                fragmentViewModel.removePhotoFromEms(photoItem, emsItem)
            }
        )

        vessel_ems_recycler.adapter = emsAdapter

        fragmentViewModel.vesselItemLiveData.observe(
            viewLifecycleOwner,
            Observer(::renderVesselInfo)
        )

        fragmentViewModel.deliveryItemItemLiveData.observe(
            viewLifecycleOwner,
            Observer(::renderDeliveryInfo)
        )

        fragmentViewModel.emsLiveData.observe(viewLifecycleOwner, Observer {
            displayEms(it)
        })

        fragmentViewModel.itemClicksLiveData.observe(viewLifecycleOwner, EventObserver {
            handleItemClick(it)
        })
    }

    fun fillVesselInfo(vesselToPrefill: Boat) {
        fragmentViewModel.fillVesselInfo(vesselToPrefill)
    }

    private fun renderVesselInfo(vesselItem: VesselItem) {
        fragmentBinding.vesselEditInfo.setVisible(vesselItem.inEditMode)
        fragmentBinding.vesselEditInfo.vessel_note_layout.setVisible(vesselItem.attachments.hasNotes())

        fragmentBinding.vesselViewInfo.root.setVisible(!vesselItem.inEditMode)
        fragmentBinding.vesselViewInfo.vesselViewAttachments.attachmentNoteGroup
            .setVisible(vesselItem.attachments.hasNotes())

        fragmentBinding.btnVesselInfoEdit.setVisible(!vesselItem.inEditMode)
        fragmentBinding.btnVesselAddAttachment.setVisible(vesselItem.inEditMode)
        fragmentBinding.btnVesselAddAttachment.setOnClickListener {
            askForAttachmentType(
                onNoteSelected = fragmentViewModel::addNoteForVessel,
                onPhotoSelected = fragmentViewModel::addPhotoForVessel
            )
        }

        fragmentBinding.vesselEditPhotosLayout.onPhotoClickListener = ::showFullImage

        fragmentBinding.vesselViewInfo.vesselViewAttachments.attachmentsPhotos.onPhotoClickListener =
            ::showFullImage

        fragmentBinding.vesselEditPhotosLayout.onPhotoRemoveListener = {
            fragmentViewModel.removePhotoFromVessel(it)
        }
    }

    private fun renderDeliveryInfo(deliveryItem: DeliveryItem) {
        fragmentBinding.deliveryEditLayout.setVisible(deliveryItem.inEditMode)
        fragmentBinding.deliveryNoteLayout.setVisible(deliveryItem.attachments.hasNotes())

        fragmentBinding.vesselViewDelivery.root.setVisible(!deliveryItem.inEditMode)
        fragmentBinding.vesselViewDelivery.deliveryViewAttachments.attachmentNoteGroup
            .setVisible(deliveryItem.attachments.hasNotes())

        fragmentBinding.btnVesselDeliveryEdit.setVisible(!deliveryItem.inEditMode)
        fragmentBinding.btnVesselDeliveryAttachment.setVisible(deliveryItem.inEditMode)
        fragmentBinding.btnVesselDeliveryAttachment.setOnClickListener {
            askForAttachmentType(
                onNoteSelected = fragmentViewModel::addNoteForDelivery,
                onPhotoSelected = fragmentViewModel::addPhotoForDelivery
            )
        }

        fragmentBinding.deliveryEditPhotosLayout.onPhotoClickListener = ::showFullImage

        fragmentBinding.vesselViewDelivery.deliveryViewAttachments.attachmentsPhotos.onPhotoClickListener =
            ::showFullImage

        fragmentBinding.deliveryEditPhotosLayout.onPhotoRemoveListener = {
            fragmentViewModel.removePhotoFromDelivery(it)
        }
    }

    private fun handleItemClick(itemId: Int) {
        when (itemId) {
            R.id.vessel_flag_state -> {
                val bundle =
                    bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to SimpleSearchFragment.SearchFlagState)
                navigation.navigate(R.id.action_tabsFragment_to_simple_search, bundle)
            }

            R.id.delivery_business -> {
                if (!fragmentViewModel.isNewBusiness) {
                    val bundle =
                        bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to ComplexSearchFragment.SearchBusiness)
                    navigation.navigate(R.id.action_tabsFragment_to_complex_search, bundle)
                }
            }

            R.id.delivery_edit_date -> {
                delivery_edit_date.clearFocus()
                peekDeliveryDate()
            }

            R.id.btn_next -> {
                if (isFieldCheckPassed || validateForms()) {
                    onNextListener.onNextClicked()
                } else {
                    showSnackbarWarning()
                }
            }
        }
    }

    private fun displayEms(emsItems: List<EMSItem>) {
        emsAdapter.setItems(emsItems.map { it.copy() })
    }

    private fun subscribeToSearchResult() {
        val navBackStackEntry = navigation.currentBackStackEntry!!
        navBackStackEntry.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            val savedState = navBackStackEntry.savedStateHandle
            val resultEntityType = savedState.get<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)
                ?: return@LifecycleEventObserver

            when (resultEntityType) {
                is SimpleSearchFragment.SearchEms -> {
                    val emsType =
                        savedState.remove<String>(BaseSearchFragment.SEARCH_RESULT).orEmpty()
                    if (pendingEmsForType != null) {
                        fragmentViewModel.updateEmsType(pendingEmsForType!!, emsType)
                        pendingEmsForType = null
                    }
                    savedState.remove<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)
                }
                is SimpleSearchFragment.SearchFlagState -> {
                    val flagState =
                        savedState.remove<String>(BaseSearchFragment.SEARCH_RESULT).orEmpty()
                    fragmentViewModel.updateVesselFlagState(flagState)
                    savedState.remove<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)
                }
                is ComplexSearchFragment.SearchBusiness -> {
                    val business =
                        savedState.remove<BusinessSearchModel>(BaseSearchFragment.SEARCH_RESULT)?.value
                    if (business != null) fragmentViewModel.updateDeliveryBusiness(business)

                    val isNewBusiness = savedState.remove<Boolean>(CREATE_NEW_BUSINESS)
                    if (isNewBusiness != null && isNewBusiness) fragmentViewModel.createNewBusiness()

                    savedState.remove<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)
                }
                else -> return@LifecycleEventObserver
            }
        })
    }

    private fun onDatePicked(pickerView: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val newDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }.time

        delivery_edit_date.setText(getString(R.string.report_date, newDate))
        fragmentViewModel.vesselItemLiveData.value?.vessel?.lastDelivery?.date = newDate
    }

    private fun peekDeliveryDate() {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val dialog = DatePickerDialog(
            requireContext(), ::onDatePicked,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }
}