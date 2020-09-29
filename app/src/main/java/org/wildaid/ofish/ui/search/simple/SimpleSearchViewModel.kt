package org.wildaid.ofish.ui.search.simple

import android.app.Application
import org.wildaid.ofish.data.OTHER
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.base.BaseSearchType
import org.wildaid.ofish.ui.search.base.BaseSearchViewModel

class SimpleSearchViewModel(val repository: Repository, application: Application) :
    BaseSearchViewModel<String>(application) {
    override fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource {
        return when (searchEntity) {
            is SimpleSearchFragment.SearchFlagState -> SimpleSearchDataSource(repository.getFlagStates())
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
            ?: emptyList()
    }

    private fun getGears(): List<String> {
        return repository.getMenuData()?.gear?.toList()
            ?: emptyList()
    }

    private fun getFisheries(): List<String> {
        return repository.getMenuData()?.fisheries?.toList()
            ?: emptyList()
    }

    private fun getActivities(): List<String> {
        return repository.getMenuData()?.activities?.toList()
            ?: emptyList()
    }

    private fun getEms(): List<String> {
        return repository.getMenuData()?.emsTypes?.toList()
            ?: emptyList()
    }

    inner class SimpleSearchDataSource(private val dataSource: List<String>) :
        BaseSearchViewModel<String>.SearchDataSource() {


        override fun initiateData() = dataSource

        override fun applyFilter(filter: String): List<String> {
            val result = dataSource.filter { it.contains(filter, true) }
            if (!(result.contains(OTHER)))
                return result.plus(OTHER)
            return result
        }
    }
}