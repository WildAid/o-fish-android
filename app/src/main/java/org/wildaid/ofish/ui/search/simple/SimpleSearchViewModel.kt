package org.wildaid.ofish.ui.search.simple

import android.app.Application
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.base.BaseSearchType
import org.wildaid.ofish.ui.search.base.BaseSearchViewModel
import org.wildaid.ofish.util.getStringArray

class SimpleSearchViewModel(repository: Repository, application: Application) : BaseSearchViewModel<String>(repository, application) {
    override fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource {
        return when (searchEntity) {
            is SimpleSearchFragment.SearchFlagState -> SimpleSearchDataSource(repository.getFlagStates(/* todo*/ null))
            is SimpleSearchFragment.SearchEms -> SimpleSearchDataSource(getStringArray(R.array.ems_array).toList())
            is SimpleSearchFragment.SearchFishery -> SimpleSearchDataSource(getStringArray(R.array.fishery_array).toList())
            is SimpleSearchFragment.SearchGear -> SimpleSearchDataSource(getStringArray(R.array.gear_array).toList())
            is SimpleSearchFragment.SearchActivity -> SimpleSearchDataSource(getStringArray(R.array.activity_array).toList())
            is SimpleSearchFragment.SearchSpecies -> SimpleSearchDataSource(getStringArray(R.array.species_array).toList())
            else -> throw IllegalArgumentException("Unsupported search type $searchEntity")
        }
    }

    inner class SimpleSearchDataSource(private val dataSource: List<String>) :
        BaseSearchViewModel<String>.SearchDataSource() {

        override fun initiateData() = dataSource

        override fun applyFilter(filter: String) = dataSource
            .filter { it.contains(filter, true) }
    }
}