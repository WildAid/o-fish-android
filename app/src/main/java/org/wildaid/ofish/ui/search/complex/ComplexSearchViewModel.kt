package org.wildaid.ofish.ui.search.complex

import android.app.Application
import kotlinx.coroutines.flow.*
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.base.BaseSearchType
import org.wildaid.ofish.ui.search.base.BaseSearchViewModel

private const val RECENT_BOARDINGS_COUNT = 5

class ComplexSearchViewModel(repository: Repository, application: Application) :
    BaseSearchViewModel<SearchModel>(application) {
    override fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource {
        return when (searchEntity) {
            is ComplexSearchFragment.SearchDrafts -> searchDrafts
            is ComplexSearchFragment.SearchBusiness -> searchBusinessDataSource
            is ComplexSearchFragment.SearchViolation -> searchViolationDataSource
            is ComplexSearchFragment.SearchRecords -> searchRecordsDataSource.apply {
                isAddAvailable = false
            }
            is ComplexSearchFragment.SearchBoardVessels -> searchRecordsDataSource.apply {
                isAddAvailable = true
            }
            is ComplexSearchFragment.SearchCrew -> searchCrewDataSource.apply {
                this.report = report
                    ?: throw IllegalArgumentException("$searchEntity is not supported with null report")
            }
            else -> throw IllegalArgumentException("Unsupported search entity $searchEntity")
        }
    }

    private val searchBusinessDataSource = object : SearchDataSource() {
        private val addSearchModel = AddSearchModel(R.string.new_business)

        override fun initiateData(): Flow<List<SearchModel>> {
            // TODO: make repo operation a flow instead
            return mutableListOf<SearchModel>().apply {
                add(addSearchModel)
                addAll(repository.getBusinessAndLocation().map { BusinessSearchModel(it) })
            }.let { searchModels ->
                flowOf(searchModels)
            }
        }

        override fun applyFilter(filter: String): Flow<List<SearchModel>> {
            // TODO: make repo operation a flow instead
            return mutableListOf<SearchModel>().apply {
                this.addAll(
                    repository.getBusinessAndLocation()
                        .filter {
                            it.first.contains(filter, true) ||
                                    it.second.contains(filter, true)
                        }.map { BusinessSearchModel(it) }
                )
                this.add(addSearchModel)
            }.let { searchModels ->
                flowOf(searchModels)
            }
        }
    }

    private val searchViolationDataSource = object : SearchDataSource() {
        override fun initiateData(): Flow<List<SearchModel>> {
            // TODO: make repo operation a flow instead
            return repository.getOffences()
                .map { offenceData ->
                    ViolationSearchModel(offenceData)
                }.let { violationSearchModels ->
                    flowOf(violationSearchModels)
                }
        }

        override fun applyFilter(filter: String): Flow<List<SearchModel>> {
            // TODO: make repo operation a flow instead
            return repository.getOffences()
                .filter { it.code.contains(filter, true) || it.explanation.contains(filter, true) }
                .map { ViolationSearchModel(it) }
                .let {
                    flowOf(it)
                }
        }
    }

    private val searchRecordsDataSource = object : SearchDataSource() {
        var isAddAvailable: Boolean = false

        private val addSearchModel = AddSearchModel(R.string.add_new_vessel)
        private var cachedAllReports = emptyList<Report>()

        override fun initiateData(): Flow<List<SearchModel>> {
            return repository.findReportsGroupedByVessel()
                .map { reports ->
                    cachedAllReports = reports

                    mutableListOf<SearchModel>().apply {
                        if (isAddAvailable) {
                            this.add(addSearchModel)
                        }
                        if (reports.isNotEmpty()) {
                            this.add(TextViewSearchModel(R.string.recently_boarded))
                        }

                        this.addAll(
                            reports.asSequence()
                                .filterNot { it.vessel?.name.isNullOrBlank() }
                                .groupBy { it.vessel?.permitNumber to it.vessel?.name }
                                .map { pair ->
                                    RecordSearchModel(
                                        pair.value.find { (it.vessel?.permitNumber to it.vessel?.name) == pair.key }?.vessel!!,
                                        pair.value.sortedByDescending { report -> report.date },
                                        repository
                                    )
                                }.take(RECENT_BOARDINGS_COUNT)
                        )
                    }
                }
        }

        override fun applyFilter(filter: String): Flow<List<SearchModel>> {
            return if (filter.isBlank()) {
                initiateData()
            } else {
                repository.findReportsGroupedByVessel()
                    .map { reports ->
                        if (cachedAllReports.isNullOrEmpty()) {
                            cachedAllReports = reports
                        }

                        mutableListOf<SearchModel>().apply {
                            if (isAddAvailable) {
                                this.add(addSearchModel)
                            }

                            this.addAll(
                                cachedAllReports.filterNot { it.vessel?.name.isNullOrBlank() }
                                    .filter { it.vessel?.name.orEmpty().contains(filter, true) }
                                    .groupBy { it.vessel?.permitNumber to it.vessel?.name }
                                    .map { pair ->
                                        RecordSearchModel(
                                            pair.value.find { (it.vessel?.permitNumber to it.vessel?.name) == pair.key }?.vessel!!,
                                            pair.value.sortedByDescending { report -> report.date },
                                            repository
                                        )
                                    }
                            )
                        }
                    }
            }
        }
    }

    private val searchDrafts = object : SearchDataSource() {
        private var cachedDraftBoardings = emptyList<Report>()

        override fun initiateData(): Flow<List<SearchModel>> {
            return repository.findDraftsGroupedByOfficerNameAndEmail()
                .onEach { draftBoardings ->
                    if (cachedDraftBoardings.isNullOrEmpty()) {
                        this.cachedDraftBoardings = draftBoardings
                    }
                }.map {
                    mutableListOf<SearchModel>().apply {
                        this.addAll(
                            cachedDraftBoardings.asSequence()
                                .map {
                                    RecordSearchModel(
                                        it.vessel!!,
                                        listOf(it).sortedByDescending { report -> report.date },
                                        repository
                                    )
                                }
                        )
                    }
                }
        }

        override fun applyFilter(filter: String): Flow<List<SearchModel>> {
            return mutableListOf<SearchModel>().apply {
                if (filter.isBlank()) {
                    return initiateData()
                }

                this.addAll(
                    cachedDraftBoardings.filter {
                        it.vessel?.name.orEmpty().contains(filter, true)
                    }
                        .map {
                            RecordSearchModel(
                                it.vessel!!,
                                listOf(it).sortedByDescending { report -> report.date },
                                repository
                            )
                        }
                )
            }.let { searchModels ->
                flowOf(searchModels)
            }
        }
    }

    private val searchCrewDataSource = object : SearchDataSource() {
        lateinit var report: Report

        private val addSearchModel = AddSearchModel(R.string.add_crew_member)

        override fun initiateData(): Flow<List<SearchModel>> {
            // TODO: make repo operation a flow instead
            return mutableListOf<SearchModel>().apply {
                val captain = report.captain
                if (isCaptainNotNullOrBlank(captain)) {
                    this.add(CrewSearchModel(captain!!, true))
                }

                this.addAll(
                    report.crew
                        .filter { it.name.isNotBlank() || it.license.isNotBlank() }
                        .map { member -> CrewSearchModel(member, false) }
                )
                this.add(addSearchModel)
            }.let { searchModels ->
                flowOf(searchModels)
            }
        }

        override fun applyFilter(filter: String): Flow<List<SearchModel>> {
            // TODO: make repo operation a flow instead
            return mutableListOf<SearchModel>().apply {
                val captain = report.captain
                if (isCaptainNotNullOrBlank(captain) && containFilter(captain!!, filter)) {
                    this.add(CrewSearchModel(captain, true))
                }
                this.addAll(
                    report.crew
                        .filter { it.name.isNotBlank() || it.license.isNotBlank() }
                        .filter { member -> containFilter(member, filter) }
                        .map { member -> CrewSearchModel(member, false) }
                )
                this.add(addSearchModel)
            }.let { searchModels ->
                flowOf(searchModels)
            }
        }

        private fun isCaptainNotNullOrBlank(captain: CrewMember?) =
            !captain?.name.isNullOrBlank() && !captain?.license.isNullOrBlank()

        private fun containFilter(member: CrewMember, filter: String) =
            member.name.contains(filter, true) || member.license.contains(filter, true)
    }
}