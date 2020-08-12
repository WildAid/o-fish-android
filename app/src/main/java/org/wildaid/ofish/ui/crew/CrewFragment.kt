package org.wildaid.ofish.ui.crew

import android.os.Bundle
import android.view.View
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_crew.*
import kotlinx.android.synthetic.main.item_crew_member.view.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentCrewBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.ui.base.CARDS_OFFSET_SIZE
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard
import org.wildaid.ofish.util.setVisible

class CrewFragment : BaseReportFragment(R.layout.fragment_crew) {
    private val fragmentViewModel: CrewViewModel by viewModels { getViewModelFactory() }
    private lateinit var crewAdapter: CrewAdapter
    private lateinit var viewDataBinding: FragmentCrewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initCrewMembers(currentReport, currentReportPhotos)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentCrewBinding.bind(view).apply {
            this.viewModel = fragmentViewModel
            this.lifecycleOwner = this@CrewFragment.viewLifecycleOwner
        }

        initUI()

        fragmentViewModel.crewMembersData.observe(viewLifecycleOwner, Observer {
            displayCrewMembers(it)
        })

        fragmentViewModel.canAddNewMemberData.observe(viewLifecycleOwner, Observer {
            crew_add_member_footer.setVisible(it)
        })

        fragmentViewModel.buttonIdData.observe(
            viewLifecycleOwner, EventObserver(::onButtonClicked)
        )
    }

    private fun initUI() {
        crewAdapter = CrewAdapter(
            crewRemoveListener = {
                requireActivity().currentFocus?.clearFocus()
                fragmentViewModel.removeCrewMember(it)
            },
            crewEditModeListener = {
                requireActivity().currentFocus?.clearFocus()
                fragmentViewModel.editCrewMember(it)
            },
            crewChangeListener = fragmentViewModel::onCrewMemberChanged,
            crewAddAttachmentListener = { crewItem ->
                askForAttachmentType(
                    onNoteSelected = {
                        fragmentViewModel.addNoteForMember(crewItem)
                    },
                    onPhotoSelected = {
                        fragmentViewModel.addPhotoForMember(it, crewItem)
                    })
            },
            crewOnPhotoClickListener = ::showFullImage,
            crewRemoveNoteListener = fragmentViewModel::removeNoteFromMember,
            crewRemovePhotoListener = fragmentViewModel::removePhotoFromMember
        )

        crew_recycler.apply {
            adapter = crewAdapter
            addItemDecoration(VerticalSpaceItemDecoration(CARDS_OFFSET_SIZE))
        }

        crew_add_member_footer.setOnClickListener {
            requireActivity().currentFocus?.clearFocus()
            fragmentViewModel.addCrewMember()
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentViewModel.updateCrewMembersIfNeeded()
        updateFieldsToCheckIfNeeded()
    }

    private fun displayCrewMembers(crew: List<CrewMemberItem>) {
        crewAdapter.setItems(crew.map { it.copy() })
    }

    private fun onButtonClicked(buttonId: Int) {
        hideKeyboard()
        when (buttonId) {
            R.id.btn_next -> {
                if (isFieldCheckPassed || validateForms()) {
                    onNextListener.onNextClicked()
                } else {
                    showSnackbarWarning()
                }
            }
        }
    }

    private fun updateFieldsToCheckIfNeeded() {
        if (requiredFields.isEmpty() && crew_recycler.childCount > 0) {
            val captainView = crew_recycler[0]
            requiredFields = arrayOf(
                captainView.crew_member_edit_name_layout,
                captainView.crew_member_edit_license_layout
            )
        }
    }
}