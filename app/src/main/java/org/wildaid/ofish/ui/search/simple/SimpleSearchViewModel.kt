package org.wildaid.ofish.ui.search.simple

import android.app.Application
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.base.BaseSearchType
import org.wildaid.ofish.ui.search.base.BaseSearchViewModel
import org.wildaid.ofish.util.getStringArray

class SimpleSearchViewModel(repository: Repository, application: Application) :
    BaseSearchViewModel<String>(repository, application) {
    override fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource {
        return when (searchEntity) {
            is SimpleSearchFragment.SearchFlagState -> SimpleSearchDataSource(
                repository.getFlagStates(/* todo*/null)
            )
            is SimpleSearchFragment.SearchEms -> SimpleSearchDataSource(getEms())
            is SimpleSearchFragment.SearchFishery -> SimpleSearchDataSource(getFisheries())
            is SimpleSearchFragment.SearchGear -> SimpleSearchDataSource(getGears())
            is SimpleSearchFragment.SearchActivity -> SimpleSearchDataSource(getActivities())
            is SimpleSearchFragment.SearchSpecies -> SimpleSearchDataSource(getSpecies())
            else -> throw IllegalArgumentException("Unsupported search type $searchEntity")
        }
    }

    private fun getSpecies(): List<String> {
        return repository.getMenuData()?.species?.toList()
            ?: getStringArray(R.array.species_array).toList()
    }

    private fun getGears(): List<String> {
        return repository.getMenuData()?.gear?.toList()
            ?: getStringArray(R.array.gear_array).toList()
    }

    private fun getFisheries(): List<String> {
        return repository.getMenuData()?.fisheries?.toList()
            ?: getStringArray(R.array.fishery_array).toList()
    }

    private fun getActivities(): List<String> {
        return repository.getMenuData()?.activities?.toList()
            ?: getStringArray(R.array.activity_array).toList()
    }

    private fun getEms(): List<String> {
        return repository.getMenuData()?.emsTypes?.toList()
            ?: getStringArray(R.array.ems_array).toList()
    }

    inner class SimpleSearchDataSource(private val dataSource: List<String>) :
        BaseSearchViewModel<String>.SearchDataSource() {

        override fun initiateData() = dataSource

        override fun applyFilter(filter: String) = dataSource
            .filter { it.contains(filter, true) }
    }
}