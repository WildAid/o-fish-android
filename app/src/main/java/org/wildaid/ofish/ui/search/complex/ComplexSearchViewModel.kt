package org.wildaid.ofish.ui.search.complex

import android.app.Application
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.base.BaseSearchType
import org.wildaid.ofish.ui.search.base.BaseSearchViewModel

private const val FIND_RECORDS_LIMIT = 5

class ComplexSearchViewModel(repository: Repository, application: Application) :
    BaseSearchViewModel<SearchModel>(repository, application) {
    override fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource {
        return when (searchEntity) {
            is ComplexSearchFragment.SearchBusiness -> searchBusinessDataSource
            is ComplexSearchFragment.SearchViolation -> searchViolationDataSource
            is ComplexSearchFragment.SearchRecords -> searchRecordsDataSource.apply {
                limit = FIND_RECORDS_LIMIT
            }
            is ComplexSearchFragment.SearchVessels -> searchRecordsDataSource.apply {
                isAddAvailable = true
            }
            is ComplexSearchFragment.SearchCrew -> searchCrewDataSource.apply {
                this.report = report
            }
            else -> throw IllegalArgumentException("Unsupported search entity $searchEntity")
        }
    }

    private val searchBusinessDataSource = object : SearchDataSource() {
        private val addSearchModel = AddSearchModel(R.string.new_business)

        override fun initiateData(): List<SearchModel> {
            val list = mutableListOf<SearchModel>()
            list.addAll(repository.getBusinessAndLocation().map { BusinessSearchModel(it) })
            list.add(addSearchModel)
            return list
        }

        override fun applyFilter(filter: String): List<SearchModel> {
            val list = mutableListOf<SearchModel>()
            list.addAll(repository.getBusinessAndLocation()
                .filter { it.first.contains(filter, true) || it.second.contains(filter, true) }
                .map { BusinessSearchModel(it) })
            list.add(addSearchModel)
            return list
        }
    }

    private val searchViolationDataSource = object : SearchDataSource() {
        override fun initiateData(): List<SearchModel> {
            return repository.getOffences().map { ViolationSearchModel(it) }
        }

        override fun applyFilter(filter: String): List<SearchModel> {
            return repository.getOffences()
                .filter { it.code.contains(filter, true) || it.explanation.contains(filter, true) }
                .map { ViolationSearchModel(it) }
        }
    }

    private val searchRecordsDataSource = object : SearchDataSource() {
        var isAddAvailable: Boolean = false
        var limit = 0

        private val addSearchModel = AddSearchModel(R.string.add_new_vessel)

        override fun initiateData(): List<SearchModel> {
            val list = mutableListOf<SearchModel>()
            if (limit > 0) {
                list.add(TextViewSearchModel(R.string.recently_boarded))
            }
            val allReports = repository.findAllReports()
            list.addAll(allReports
                .asSequence()
                .filterNot { it.vessel?.name.isNullOrBlank() }
                .groupBy { it.vessel?.permitNumber }
                .map { pair ->
                    RecordSearchModel(allReports.find { it.vessel?.permitNumber == pair.key }?.vessel!!,
                        pair.value.sortedByDescending { report -> report.date })
                }.take(limit)
            )
            if (isAddAvailable) list.add(addSearchModel)

            return list
        }

        override fun applyFilter(filter: String): List<SearchModel> {
            if (filter.isBlank()) {
                return if (isAddAvailable) listOf(addSearchModel) else emptyList()
            }

            val list = mutableListOf<SearchModel>()
            list.addAll(repository.findAllReports()
                .filterNot { it.vessel?.name.isNullOrBlank() }
                .filter { it.vessel?.name.orEmpty().contains(filter, true) }
                .groupBy { it.vessel }
                .map {
                    RecordSearchModel(
                        it.key!!,
                        it.value.sortedByDescending { report -> report.date })
                })
            if (isAddAvailable) list.add(addSearchModel)

            return list
        }
    }

    private val searchCrewDataSource = object : SearchDataSource() {
        var report: Report? = null
        private val addSearchModel = AddSearchModel(R.string.add_crew_member)

        override fun initiateData(): List<SearchModel> {
            val list = mutableListOf<SearchModel>()
            report?.let {
                val captain = it.captain
                if (isCaptainNotNullOrBlank(captain)) list.add(
                    CrewSearchModel(
                        captain!!,
                        true
                    )
                )
                list.addAll(it.crew.map { member -> CrewSearchModel(member, false) })
            }
            list.add(addSearchModel)
            return list
        }

        override fun applyFilter(filter: String): List<SearchModel> {
            val list = mutableListOf<SearchModel>()
            report?.let {
                val captain = it.captain
                if (isCaptainNotNullOrBlank(captain) && containFilter(captain!!, filter)) {
                    list.add(CrewSearchModel(captain, true))
                }
                list.addAll(it.crew
                    .filter { member -> containFilter(member, filter) }
                    .map { member -> CrewSearchModel(member, false) })
            }
            list.add(addSearchModel)
            return list
        }

        private fun isCaptainNotNullOrBlank(captain: CrewMember?) =
            captain != null && (captain.name.isNotBlank() || captain.license.isNotBlank())

        private fun containFilter(member: CrewMember, filter: String) =
            member.name.contains(filter, true) || member.license.contains(filter, true)
    }
}