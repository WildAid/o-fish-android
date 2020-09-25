package org.wildaid.ofish.ui.catches

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_catch.*
import kotlinx.android.synthetic.main.item_edit_catch.view.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentCatchBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.ui.base.CARDS_OFFSET_SIZE
import org.wildaid.ofish.ui.crew.VerticalSpaceItemDecoration
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.simple.SimpleSearchFragment
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard

class CatchFragment : BaseReportFragment(R.layout.fragment_catch) {
    private val fragmentViewModel: CatchViewModel by viewModels { getViewModelFactory() }

    private lateinit var viewDataBinding: FragmentCatchBinding
    private lateinit var catchAdapter: CatchAdapter
    private var pendingCatchItem: CatchItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initCatch(currentReport, currentReportPhotos)
        subscribeToSearchResult()
    }

    override fun isAllRequiredFieldsNotEmpty(): Boolean {
        catch_recycler.forEach { item ->
            if (item.weight_edit_layout.editText?.text?.isNotBlank()!! || item.count_edit_layout.editText?.text?.isNotBlank()!!) {
                return true
            }
        }
        return false
    }

    override fun validateForms(): Boolean {
        var result = false
        catch_recycler.forEach { item ->
            if (item.weight_edit_layout.editText?.text?.isNotBlank()!! || item.count_edit_layout.editText?.text?.isNotBlank()!!) {
                result = true
                validationPassed()
            } else {
                validateField(item.weight_edit_layout)
                validateField(item.count_edit_layout)
                result = false
            }
        }
        isFieldCheckPassed = true
        return result
    }

    private fun validationPassed() {
        isFieldCheckPassed = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentCatchBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = this@CatchFragment.viewLifecycleOwner
        }

        initUI()

        fragmentViewModel.catchItemsLiveData.observe(
            viewLifecycleOwner,
            Observer { displayCatch(it) })

        fragmentViewModel.catchUserEventLiveData.observe(
            viewLifecycleOwner, EventObserver(::handleUserEvent)
        )
    }

    private fun initUI() {
        catchAdapter = CatchAdapter(
            searchListener = { id: Int, item: CatchItem ->
                pendingCatchItem = item
                hideKeyboard()
                navigateToSearch(id)
            },
            catchEditModeListener = {
                hideKeyboard()
                fragmentViewModel.editCatch(it)
            },
            catchRemoveListener = {
                hideKeyboard()
                fragmentViewModel.removeCatch(it)
            },
            catchAddAttachmentListener = { catchItem ->
                askForAttachmentType(
                    onNoteSelected = {
                        fragmentViewModel.addNoteForCatch(catchItem)
                    },
                    onPhotoSelected = {
                        fragmentViewModel.addPhotoForCatch(it, catchItem)
                    }
                )
            },
            catchRemoveNoteListener = {
                fragmentViewModel.removeNoteFromCatch(it)
            },
            catchOnPhotoClickListener = ::showFullImage,
            catchRemovePhotoListener = { photo, catchItem ->
                fragmentViewModel.removePhotoFromCatch(photo, catchItem)
            })

        catch_recycler.apply {
            adapter = catchAdapter
            addItemDecoration(VerticalSpaceItemDecoration(CARDS_OFFSET_SIZE))
        }

        catch_add_footer.setOnClickListener {
            requireActivity().currentFocus?.clearFocus()
            fragmentViewModel.addCatch()
        }
    }

    private fun navigateToSearch(id: Int) {
        when (id) {
            R.id.species_edit_name -> {
                val bundle =
                    bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to SimpleSearchFragment.SearchSpecies)
                navigation.navigate(R.id.action_tabsFragment_to_simple_search, bundle)
            }
        }
    }

    private fun displayCatch(list: List<CatchItem>) {
        val newList = list.map { item -> item.copy() }
        catchAdapter.setItems(newList)
    }

    private fun subscribeToSearchResult() {
        val navBackStackEntry = navigation.currentBackStackEntry!!
        navBackStackEntry.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            val savedState = navBackStackEntry.savedStateHandle
            when (savedState.get<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)) {
                is SimpleSearchFragment.SearchSpecies -> {
                    val result = savedState.remove<String>(BaseSearchFragment.SEARCH_RESULT)
                    if (result != null && pendingCatchItem != null) {
                        fragmentViewModel.updateSpeciesForCatch(result, pendingCatchItem!!)
                    }
                    pendingCatchItem = null
                }
                else -> return@LifecycleEventObserver
            }
            savedState.remove<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)
        })
    }

    private fun handleUserEvent(userEvent: CatchViewModel.CatchUserEvent) {
        hideKeyboard()
        when (userEvent) {
            CatchViewModel.CatchUserEvent.Next -> {
                if (isFieldCheckPassed || validateForms()) {
                    onNextListener.onNextClicked()
                } else {
                    showSnackbarWarning()
                    validationPassed()
                }
            }
        }
    }

}