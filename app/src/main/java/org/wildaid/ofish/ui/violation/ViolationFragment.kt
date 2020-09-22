package org.wildaid.ofish.ui.violation

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_violation.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentViolationBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.ui.base.CARDS_OFFSET_SIZE
import org.wildaid.ofish.ui.crew.VerticalSpaceItemDecoration
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment
import org.wildaid.ofish.ui.search.complex.CrewSearchModel
import org.wildaid.ofish.ui.search.complex.ViolationSearchModel
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard
import org.wildaid.ofish.util.setVisible

class ViolationFragment : BaseReportFragment(R.layout.fragment_violation) {
    private val fragmentViewModel: ViolationViewModel by viewModels { getViewModelFactory() }
    private var pendingItemId: String? = null

    private lateinit var viewDataBinding: FragmentViolationBinding
    private lateinit var violationAdapter: ViolationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initViolations(currentReport, currentReportPhotos)
        subscribeToSearchResult()
    }

    override fun isAllRequiredFieldsNotEmpty(): Boolean {
        return true
    }

    override fun validateForms(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        fragmentViewModel.refreshIssuedTo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentViolationBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = viewLifecycleOwner
        }

        initUI()

        fragmentViewModel.violationLiveData.observe(
            viewLifecycleOwner,
            Observer { displayViolations(it) })

        fragmentViewModel.seizureLiveData.observe(
            viewLifecycleOwner,
            Observer { displaySeizure(it) })

        fragmentViewModel.violationUserEventLiveData.observe(
            viewLifecycleOwner, EventObserver(::handleUserEvent)
        )
    }

    private fun initUI() {
        violationAdapter = ViolationAdapter(
            captain = currentReport.captain,
            violationSearchListener = { id: Int, violationItem: ViolationItem ->
                pendingItemId = violationItem.title
                navigateToSearch(id)
            },
            violationAttachmentListener = { item ->
                askForAttachmentType(
                    onNoteSelected = {
                        fragmentViewModel.addNoteForViolation(item)
                    },
                    onPhotoSelected = {
                        fragmentViewModel.addPhotoForViolation(it, item)
                    }
                )
            },
            violationEditModeListener = fragmentViewModel::editViolation,
            violationRemoveListener = {
                hideKeyboard()
                fragmentViewModel.removeViolation(it)
            },
            violationOnPhotoClickListener = ::showFullImage,
            violationRemovePhotoListener = fragmentViewModel::removePhotoFromViolation,
            violationRemoveNoteListener = fragmentViewModel::removeNoteFromViolation
        )

        violation_recycler.apply {
            adapter = violationAdapter
            addItemDecoration(VerticalSpaceItemDecoration(CARDS_OFFSET_SIZE))
        }

        violation_add_footer.setOnClickListener {
            requireActivity().currentFocus?.clearFocus()
            fragmentViewModel.addViolation()
        }

        viewDataBinding.violationSeizuresAttach.setOnClickListener {
            askForAttachmentType(
                onNoteSelected = {
                    fragmentViewModel.addNoteForSeizure()
                },
                onPhotoSelected = {
                    fragmentViewModel.addPhotoForSeizure(it)
                }
            )
        }

        viewDataBinding.seizureEditPhotosLayout.onPhotoRemoveListener = {
            fragmentViewModel.removePhotoFromSeizure(it)
        }

        viewDataBinding.seizureEditPhotosLayout.onPhotoClickListener = ::showFullImage
    }

    private fun navigateToSearch(id: Int) {
        val pairs = when (id) {
            R.id.violation_edit_name -> BaseSearchFragment.SEARCH_ENTITY_KEY to ComplexSearchFragment.SearchViolation
            R.id.issued_edit_name -> BaseSearchFragment.SEARCH_ENTITY_KEY to ComplexSearchFragment.SearchCrew
            else -> throw IllegalArgumentException("Illegal Search type")
        }
        val bundle = bundleOf(pairs)
        navigation.navigate(R.id.action_tabsFragment_to_complex_search, bundle)
    }

    private fun displayViolations(list: List<ViolationItem>) {
        val newList = list.map { item -> item.copy() }
        violationAdapter.setItems(newList)
    }

    private fun displaySeizure(seizureItem: SeizureItem) {
        viewDataBinding.violationSeizureNoteLayout.setVisible(seizureItem.attachments.hasNotes())
    }

    private fun subscribeToSearchResult() {
        val navBackStackEntry = navigation.currentBackStackEntry!!
        navBackStackEntry.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            val savedState = navBackStackEntry.savedStateHandle
            when (savedState.get<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)) {
                is ComplexSearchFragment.SearchViolation -> {
                    val result =
                        savedState.remove<ViolationSearchModel>(BaseSearchFragment.SEARCH_RESULT)
                    if (result != null) fragmentViewModel.updateViolationExplanation(
                        pendingItemId,
                        result.value
                    )
                    pendingItemId = null
                }
                is ComplexSearchFragment.SearchCrew -> {
                    val result =
                        savedState.remove<CrewSearchModel>(BaseSearchFragment.SEARCH_RESULT)
                    if (result != null) fragmentViewModel.updateIssuedTo(
                        pendingItemId,
                        result.value
                    )
                    pendingItemId = null
                }
                else -> return@LifecycleEventObserver
            }
            savedState.remove<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)
        })
    }

    private fun handleUserEvent(event: ViolationViewModel.ViolationUserEvent) {
        hideKeyboard()
        when (event) {
            ViolationViewModel.ViolationUserEvent.NextEvent -> {
                onNextListener.onNextClicked()
            }
        }
    }
}