package org.wildaid.ofish.ui.search.complex

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.realm.RealmList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.data.OffenceData
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Boat
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.base.BaseSearchType

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ComplexSearchViewModelTest {
    @MockK
    private lateinit var mockedRepository: Repository

    private lateinit var complexSearchViewModel: ComplexSearchViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        complexSearchViewModel =
            ComplexSearchViewModel(mockedRepository, ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testBusinessDataSource() {
        val initiateBusiness = listOf(
            Pair("Business1", "Location1"),
            Pair("Business2", "Location2"),
            Pair("Business3", "Location3"),
            Pair("Business4", "Location4"),
            Pair("Business5", "Location5")
        )

        every { mockedRepository.getBusinessAndLocation() } returns initiateBusiness

        val searchDataSource =
            complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchBusiness, null)

        runBlockingTest {
            val result = searchDataSource.initiateData()
                .first()

            assert(result.size == initiateBusiness.size + 1)
            assert(result.first() is BusinessSearchModel)
            assert(
                (result.first() as BusinessSearchModel).value == initiateBusiness.first()
            )
            assert(result.last() is AddSearchModel)

            val filter = "3"
            val filteredBusiness = listOf(Pair("Business3", "Location3"))

            val filterResult = searchDataSource.applyFilter(filter)
                .first()
            assert(filterResult.size == filteredBusiness.size + 1)
            assert(filterResult.first() is BusinessSearchModel)
            assert(
                (filterResult.first() as BusinessSearchModel).value == filteredBusiness.first()
            )
            assert(filterResult.last() is AddSearchModel)
        }
    }

    @Test
    fun testViolationDataSource() {
        val initiateOffence = listOf(
            OffenceData("Some code 1", "explanation 1"),
            OffenceData("Some code 2", "explanation 2"),
            OffenceData("Some code 3", "explanation 3"),
            OffenceData("Some code 4", "explanation 4"),
            OffenceData("Some code 5", "explanation 5")
        )

        every { mockedRepository.getOffences() } returns initiateOffence

        val searchDataSource =
            complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchViolation, null)

        runBlockingTest {
            val results = searchDataSource.initiateData()
                .first()

            assert(results.size == initiateOffence.size)
            assert(results.first() is ViolationSearchModel)
            assert(
                (results.first() as ViolationSearchModel).value == initiateOffence.first()
            )

            val filter = "3"
            val filteredOffence = listOf(OffenceData("Some code 3", "explanation 3"))
            val filterResults = searchDataSource.applyFilter(filter)
                .first()
            assert(filterResults.size == filteredOffence.size)
            assert(filterResults.first() is ViolationSearchModel)
            assert(
                (filterResults.first() as ViolationSearchModel).value == filteredOffence.first()
            )
        }
    }

    @Test
    fun testRecordsDataSource() {
        val initiateGroupedReports = listOf(
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 1"
                    permitNumber = "Vessel permit 1"
                }
            },
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 2"
                    permitNumber = "Vessel permit 2"
                }
            },
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 3"
                    permitNumber = "Vessel permit 3"
                }
            },
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 4"
                    permitNumber = "Vessel permit 4"
                }
            },
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 5"
                    permitNumber = "Vessel permit 5"
                }
            }
        )

        every { mockedRepository.findReportsGroupedByVessel() } returns flowOf(initiateGroupedReports)

        val searchDataSource =
            complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchRecords, null)

        runBlockingTest {
            val results = searchDataSource.initiateData()
                .first()
            assert(results.size == initiateGroupedReports.size + 1)
            assert(results.first() is TextViewSearchModel)
            assert(results[1] is RecordSearchModel)
            assert(
                (results.last() as RecordSearchModel).vessel.name == initiateGroupedReports.last().vessel?.name
            )
            assert(
                (results.last() as RecordSearchModel).vessel.permitNumber == initiateGroupedReports.last().vessel?.permitNumber
            )

            val filter = "3"
            val filteredRecords = listOf(Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 3"
                    permitNumber = "Vessel permit 3"
                }
            })

            val filterResults = searchDataSource.applyFilter(filter)
                .first()
            assert(filterResults.size == filteredRecords.size)
            assert(filterResults.first() is RecordSearchModel)
            assert(
                (filterResults.last() as RecordSearchModel).vessel.name == filteredRecords.last().vessel?.name
            )
            assert(
                (filterResults.last() as RecordSearchModel).vessel.permitNumber == filteredRecords.last().vessel?.permitNumber
            )
        }
    }

    @Test
    fun testBoardVesselsDataSource() {
        val initiateGroupedReports = listOf(
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 1"
                    permitNumber = "Vessel permit 1"
                }
            },
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 2"
                    permitNumber = "Vessel permit 2"
                }
            },
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 3"
                    permitNumber = "Vessel permit 3"
                }
            },
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 4"
                    permitNumber = "Vessel permit 4"
                }
            },
            Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 5"
                    permitNumber = "Vessel permit 5"
                }
            }
        )

        every { mockedRepository.findReportsGroupedByVessel() } returns flowOf(initiateGroupedReports)

        val searchDataSource =
            complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchBoardVessels, null)

        runBlockingTest {
            val initiateData = searchDataSource.initiateData()
                .first()
            assert(initiateData.size == initiateGroupedReports.size + 2)
            assert(initiateData[0] is AddSearchModel)
            assert(initiateData[1] is TextViewSearchModel)
            assert(
                (initiateData.last() as RecordSearchModel).vessel.name
                        == initiateGroupedReports.last().vessel?.name
            )
            assert(
                (initiateData.last() as RecordSearchModel).vessel.permitNumber
                        == initiateGroupedReports.last().vessel?.permitNumber
            )

            val filter = "3"
            val filteredRecords = listOf(Report().apply {
                vessel = Boat().apply {
                    name = "Vessel name 3"
                    permitNumber = "Vessel permit 3"
                }
            })
            val filterResults = searchDataSource.applyFilter(filter)
                .first()
            assert(filterResults.size == filteredRecords.size + 1)
            assert(filterResults.first() is AddSearchModel)
            assert(
                (filterResults.last() as RecordSearchModel).vessel.name == filteredRecords.last().vessel?.name
            )
            assert(
                (filterResults.last() as RecordSearchModel).vessel.permitNumber == filteredRecords.last().vessel?.permitNumber
            )
        }
    }

    @Test
    fun testEmptyCrewDataSource() {
        runBlockingTest {
            val mockedReport = Report()
            val searchDataSource =
                complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchCrew, mockedReport)

            val results = searchDataSource.initiateData()
                .first()
            assert(results.size == 1)
            assert(results.first() is AddSearchModel)
        }
    }

    @Test
    fun testCrewDataSource() {
        runBlockingTest {
            val mockedReport = Report().apply {
                captain = CrewMember().apply {
                    name = "Captain"
                    license = "Captain license"
                }

                crew = RealmList(
                    CrewMember().apply {
                        name = "Crew 1"
                        license = "Crew license 1"
                    },
                    CrewMember().apply {
                        name = "Crew 2"
                        license = "Crew license 2"
                    },
                    CrewMember().apply {
                        name = "Crew 3"
                        license = "Crew license 3"
                    },
                    CrewMember().apply {
                        name = "Crew 4"
                        license = "Crew license 4"
                    },
                    CrewMember().apply {
                        name = "Crew 5"
                        license = "Crew license 5"
                    }
                )
            }

            val searchDataSource =
                complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchCrew, mockedReport)

            val initiateCrew = listOf(mockedReport.captain, *mockedReport.crew.toArray())

            val results = searchDataSource.initiateData()
                .first()
            assert(results.size == initiateCrew.size + 1)
            assert(results.first() is CrewSearchModel)
            assert(results.last() is AddSearchModel)

            val filter = "3"
            val filteredCrew = listOf(
                CrewMember().apply {
                    name = "Crew 3"
                    license = "Crew license 3"
                }
            )

            val filterResults = searchDataSource.applyFilter(filter)
                .first()
            assert(filterResults.size == filteredCrew.size + 1)
            assert(filterResults.first() is CrewSearchModel)
            assert(
                !(filterResults.first() as CrewSearchModel).isCaptain
            )
            assert(
                (filterResults.first() as CrewSearchModel).value.name == filteredCrew.first().name
            )
            assert(
                (filterResults.first() as CrewSearchModel).value.license == filteredCrew.first().license
            )
            assert(filterResults.last() is AddSearchModel)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidSearchType() {
        complexSearchViewModel.getDataSource(BaseSearchType(), null)
    }
}