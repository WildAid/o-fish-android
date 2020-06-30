package org.wildaid.ofish.ui.violation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentAddCrewBinding
import org.wildaid.ofish.ui.createreport.CreateReportViewModel
import org.wildaid.ofish.ui.search.base.BaseSearchFragment.Companion.SEARCH_ENTITY_KEY
import org.wildaid.ofish.ui.search.base.BaseSearchFragment.Companion.SEARCH_RESULT
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment
import org.wildaid.ofish.ui.search.complex.CrewSearchModel
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard

class AddCrewFragment : Fragment(R.layout.fragment_add_crew) {

    private val fragmentViewModel: AddCrewViewModel by viewModels { getViewModelFactory() }
    private val activityViewModel: CreateReportViewModel by activityViewModels { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentAddCrewBinding
    private lateinit var navigation: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation = findNavController()
        fragmentViewModel.initReport(activityViewModel.report)
    }

    override fun onStart() {
        super.onStart()
        activityViewModel.isAddingCrewMember = true
    }

    override fun onStop() {
        super.onStop()
        activityViewModel.isAddingCrewMember = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentAddCrewBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = this@AddCrewFragment.viewLifecycleOwner
        }

        fragmentViewModel.crewMember.observe(
            viewLifecycleOwner, EventObserver(::onCrewAdded)
        )

        fragmentViewModel.validated.observe(
            viewLifecycleOwner, EventObserver(::onButtonClicked)
        )
    }

    private fun onCrewAdded(crewMember: CrewSearchModel) {
        hideKeyboard()
        navigation.getBackStackEntry(R.id.tabsFragment).apply {
            savedStateHandle.set(
                SEARCH_RESULT,
                crewMember
            )
            savedStateHandle.set(SEARCH_ENTITY_KEY, ComplexSearchFragment.SearchCrew)
        }
        navigation.popBackStack(R.id.tabsFragment, false)

    }

    private fun onButtonClicked(validated: Boolean) {
        hideKeyboard()
        if (!validated) {
            Snackbar.make(
                requireView(),
                getString(R.string.validation_error),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}
