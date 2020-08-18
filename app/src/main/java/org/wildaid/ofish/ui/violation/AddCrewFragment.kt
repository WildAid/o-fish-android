package org.wildaid.ofish.ui.violation

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_add_crew.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentAddCrewBinding
import org.wildaid.ofish.ui.base.ConfirmationDialogFragment
import org.wildaid.ofish.ui.base.DIALOG_CLICK_EVENT
import org.wildaid.ofish.ui.base.DialogButton
import org.wildaid.ofish.ui.base.DialogClickEvent
import org.wildaid.ofish.ui.createreport.CreateReportViewModel
import org.wildaid.ofish.ui.search.base.BaseSearchFragment.Companion.SEARCH_ENTITY_KEY
import org.wildaid.ofish.ui.search.base.BaseSearchFragment.Companion.SEARCH_RESULT
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment
import org.wildaid.ofish.ui.search.complex.CrewSearchModel
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard

const val OVERRIDE_CAPTAIN_DIALOG_ID = 1802

class AddCrewFragment : Fragment(R.layout.fragment_add_crew) {

    private val fragmentViewModel: AddCrewViewModel by viewModels { getViewModelFactory() }
    private val activityViewModel: CreateReportViewModel by activityViewModels { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentAddCrewBinding
    private lateinit var navigation: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation = findNavController()
        fragmentViewModel.initReport(activityViewModel.report)
        subscribeToDialogEvents()
        this.setHasOptionsMenu(true)
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
        add_crew_toolbar.setNavigationIcon(R.drawable.ic_close_white)
        (requireActivity() as AppCompatActivity).setSupportActionBar(add_crew_toolbar)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> navigation.popBackStack()
        }

        return super.onOptionsItemSelected(item)
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

    private fun onButtonClicked(validated: AddCrewValidation) {
        hideKeyboard()
        when (validated) {
            AddCrewValidation.NOT_VALID -> {
                Snackbar.make(
                    requireView(),
                    getString(R.string.validation_error),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            AddCrewValidation.IS_CAPTAIN -> {
                showOverrideCaptainDialog()
            }
        }
    }

    private fun showOverrideCaptainDialog() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            OVERRIDE_CAPTAIN_DIALOG_ID,
            getString(R.string.report_already_contains_captain),
            getString(R.string.replace_captain),
            getString(R.string.yes),
            getString(R.string.continue_editing)
        ).bundle()

        navigation.navigate(R.id.action_add_crew_fragment_to_confirmation_dialog, dialogBundle)
    }

    private fun subscribeToDialogEvents() {
        val navStack = navigation.currentBackStackEntry!!
        navStack.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            if (navStack.savedStateHandle.contains(DIALOG_CLICK_EVENT)) {
                val click = navStack.savedStateHandle.get<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                val handled = handleDialogClick(click)
                if (handled) {
                    navStack.savedStateHandle.remove<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                }
            }
        })
    }

    private fun handleDialogClick(event: DialogClickEvent): Boolean {
        return when (event.dialogId) {
            OVERRIDE_CAPTAIN_DIALOG_ID -> {
                if (event.dialogBtn == DialogButton.POSITIVE) fragmentViewModel.updateCaptain()
                true
            }
            else -> false
        }
    }
}