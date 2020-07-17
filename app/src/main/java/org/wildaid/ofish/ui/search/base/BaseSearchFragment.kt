package org.wildaid.ofish.ui.search.base

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_search.*
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.base.ProgressDialogFragment
import org.wildaid.ofish.ui.createreport.CreateReportActivity
import org.wildaid.ofish.ui.createreport.CreateReportViewModel
import org.wildaid.ofish.ui.reportdetail.KEY_REPORT_ID
import org.wildaid.ofish.ui.search.complex.*
import org.wildaid.ofish.ui.vessel.CREATE_NEW_BUSINESS
import org.wildaid.ofish.ui.vesseldetails.KEY_VESSEL_PERMIT_NUMBER
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard
import org.wildaid.ofish.util.showKeyboard
import java.io.Serializable

abstract class BaseSearchFragment<T> : Fragment(R.layout.fragment_search) {
    protected lateinit var currentSearchEntity: BaseSearchType
    protected val baseSearchViewModel: BaseSearchViewModel<T> by lazy { createViewModel() }
    protected val activityViewModel: CreateReportViewModel by activityViewModels { getViewModelFactory() }

    private val navigation: NavController by lazy { findNavController() }
    private var progressDialog: ProgressDialogFragment? = null
    private lateinit var baseSearchAdapter: BaseSearchAdapter<T>
    private lateinit var searchView: SearchView

    abstract fun createAdapter(itemListener: (T) -> Unit): BaseSearchAdapter<T>
    abstract fun createViewModel(): BaseSearchViewModel<T>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentSearchEntity = arguments?.getSerializable(SEARCH_ENTITY_KEY) as BaseSearchType
    }

    override fun onStart() {
        super.onStart()
        activityViewModel.isOnSearch = true
    }

    override fun onStop() {
        super.onStop()
        activityViewModel.isOnSearch = false
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(search_toolbar)
        setHasOptionsMenu(true)

        baseSearchAdapter = createAdapter(::onItemSelected)

        search_recycler.apply {
            adapter = baseSearchAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
                setDrawable(requireContext().getDrawable(R.drawable.ic_recycler_divider)!!)
            })
        }

        val report =
            if (requireActivity() is CreateReportActivity) activityViewModel.report else null
        baseSearchViewModel.initDataList(currentSearchEntity, report)
        baseSearchViewModel.dataList.observe(viewLifecycleOwner, Observer {
            baseSearchAdapter.setItems(it)
        })

        baseSearchViewModel.progressLiveData.observe(viewLifecycleOwner, Observer {
            handleProgress(it)
        })

        search_toolbar.title = getSearchTitle()
    }

    abstract fun getSearchTitle(): String

    abstract fun getSearchHint(): String

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_search_fragment, menu)
        val searchItem = menu.findItem(R.id.menu_search)
        searchView = searchItem.actionView as SearchView

        val sm = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(sm.getSearchableInfo(requireActivity().componentName))

        searchView.queryHint = getSearchHint()
        searchView.maxWidth = Integer.MAX_VALUE;
        searchItem.expandActionView()
        searchView.setOnQueryTextListener(toolbarSearchListener)

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?) = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                navigation.popBackStack()
                return true
            }
        })
        searchView.setOnQueryTextFocusChangeListener { _,  hasFocus ->
            if (hasFocus) {
                searchView.showKeyboard()
            } else {
                searchView.hideKeyboard()
            }
        }
    }

    fun applySearchQuery(query: String?) {
        searchView.setQuery(query, false)
    }

    protected open fun onItemSelected(selectedItem: T) {
        when (selectedItem) {
            is AddSearchModel -> navigateFromAdd()
            is RecordSearchModel -> {
                val detailArgs =
                    bundleOf(KEY_VESSEL_PERMIT_NUMBER to (selectedItem as RecordSearchModel).vessel.permitNumber)
                navigation.navigate(R.id.vessel_details_fragment, detailArgs)
            }
            is DutyReportSearchModel -> {
                val navigationArgs = bundleOf(KEY_REPORT_ID to (selectedItem as DutyReportSearchModel).report._id)
                navigation.navigate(
                    R.id.action_complex_search_to_report_details_fragment,
                    navigationArgs
                )
            }
            is TextViewSearchModel -> { //Nothing
            }
            else -> {
                navigation.previousBackStackEntry?.let {
                    it.savedStateHandle.set(SEARCH_RESULT, selectedItem)
                    it.savedStateHandle.set(SEARCH_ENTITY_KEY, currentSearchEntity)
                }
                navigation.popBackStack()
            }
        }
    }

    private fun handleProgress(showProgress: Boolean) {
        if (showProgress && progressDialog == null) {
            progressDialog = ProgressDialogFragment()
            progressDialog?.show(childFragmentManager, ProgressDialogFragment::class.java.name)
        } else {
            progressDialog?.dismissAllowingStateLoss()
            progressDialog = null
        }
    }

    private fun navigateFromAdd() {
        when (currentSearchEntity) {
            is ComplexSearchFragment.SearchBoardVessels -> navigation.navigate(R.id.action_complex_search_to_create_report)
            is ComplexSearchFragment.SearchRecords -> navigation.navigate(R.id.action_complex_search_to_create_report)
            is ComplexSearchFragment.SearchCrew -> navigation.navigate(R.id.add_crew_fragment)
            is ComplexSearchFragment.SearchBusiness -> {
                navigation.previousBackStackEntry?.let {
                    it.savedStateHandle.set(CREATE_NEW_BUSINESS, true)
                    it.savedStateHandle.set(SEARCH_ENTITY_KEY, currentSearchEntity)
                }
                navigation.popBackStack()
            }
        }
    }

    private val toolbarSearchListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(newText: String?): Boolean {
            baseSearchViewModel.applyFilter(newText.orEmpty())
            showNecessaryLayout(
                baseSearchViewModel.isReportSearchEmpty(
                    currentSearchEntity,
                    newText
                ), newText
            )
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            baseSearchViewModel.applyFilter(query.orEmpty())
            showNecessaryLayout(
                baseSearchViewModel.isReportSearchEmpty(currentSearchEntity, query),
                query
            )
            return false
        }
    }

    private fun showNecessaryLayout(isSearchEmpty: Boolean, query: String?) {
        if (isSearchEmpty) {
            empty_result_layout.visibility = View.VISIBLE
            empty_result_text.text = getString(R.string.no_results_for, query)
        } else {
            empty_result_layout.visibility = View.GONE
        }
    }

    companion object {
        const val SEARCH_ENTITY_KEY = "search_entity"
        const val SEARCH_RESULT = "search_result"
    }
}

open class BaseSearchType : Serializable