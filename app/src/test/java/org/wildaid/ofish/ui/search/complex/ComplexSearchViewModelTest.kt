package org.wildaid.ofish.ui.search.complex

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.realm.RealmList
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
        val initiateBussines = listOf(
            Pair("Business1", "Location1"),
            Pair("Business2", "Location2"),
            Pair("Business3", "Location3"),
            Pair("Business4", "Location4"),
            Pair("Business5", "Location5")
        )

        every { mockedRepository.getBusinessAndLocation() } returns initiateBussines

        val searchDataSource =
            complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchBusiness, null)

        assert(searchDataSource.initiateDataBlocking().size == initiateBussines.size + 1)
        assert(searchDataSource.initiateDataBlocking().first() is BusinessSearchModel)
        assert(
            (searchDataSource.initiateDataBlocking()
                .first() as BusinessSearchModel).value == initiateBussines.first()
        )
        assert(searchDataSource.initiateDataBlocking().last() is AddSearchModel)

        val filter = "3"
        val filteredBusiness = listOf(Pair("Business3", "Location3"))
        assert(searchDataSource.applyFilter(filter).size == filteredBusiness.size + 1)
        assert(searchDataSource.applyFilter(filter).first() is BusinessSearchModel)
        assert(
            (searchDataSource.applyFilter(filter)
                .first() as BusinessSearchModel).value == filteredBusiness.first()
        )
        assert(searchDataSource.applyFilter(filter).last() is AddSearchModel)
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

        assert(searchDataSource.initiateDataBlocking().size == initiateOffence.size)
        assert(searchDataSource.initiateDataBlocking().first() is ViolationSearchModel)
        assert(
            (searchDataSource.initiateDataBlocking()
                .first() as ViolationSearchModel).value == initiateOffence.first()
        )

        val filter = "3"
        val filteredOffence = listOf(OffenceData("Some code 3", "explanation 3"))
        assert(searchDataSource.applyFilter(filter).size == filteredOffence.size)
        assert(searchDataSource.applyFilter(filter).first() is ViolationSearchModel)
        assert(
            (searchDataSource.applyFilter(filter)
                .first() as ViolationSearchModel).value == filteredOffence.first()
        )
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

        every { mockedRepository.findReportsGroupedByVesselBlocking() } returns initiateGroupedReports

        val searchDataSource =
            complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchRecords, null)

        assert(searchDataSource.initiateDataBlocking().size == initiateGroupedReports.size + 1)
        assert(searchDataSource.initiateDataBlocking().first() is TextViewSearchModel)
        assert(searchDataSource.initiateDataBlocking()[1] is RecordSearchModel)
        assert(
            (searchDataSource.initiateDataBlocking()
                .last() as RecordSearchModel).vessel.name == initiateGroupedReports.last().vessel?.name
        )
        assert(
            (searchDataSource.initiateDataBlocking()
                .last() as RecordSearchModel).vessel.permitNumber == initiateGroupedReports.last().vessel?.permitNumber
        )

        val filter = "3"
        val filteredRecords = listOf(Report().apply {
            vessel = Boat().apply {
                name = "Vessel name 3"
                permitNumber = "Vessel permit 3"
            }
        })
        assert(searchDataSource.applyFilter(filter).size == filteredRecords.size)
        assert(searchDataSource.applyFilter(filter).first() is RecordSearchModel)
        assert(
            (searchDataSource.applyFilter(filter)
                .last() as RecordSearchModel).vessel.name == filteredRecords.last().vessel?.name
        )
        assert(
            (searchDataSource.applyFilter(filter)
                .last() as RecordSearchModel).vessel.permitNumber == filteredRecords.last().vessel?.permitNumber
        )
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

        every { mockedRepository.findReportsGroupedByVesselBlocking() } returns initiateGroupedReports

        val searchDataSource =
            complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchBoardVessels, null)

        val initiateData = searchDataSource.initiateDataBlocking()
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
        assert(searchDataSource.applyFilter(filter).size == filteredRecords.size + 1)
        assert(searchDataSource.applyFilter(filter).first() is AddSearchModel)
        assert(
            (searchDataSource.applyFilter(filter)
                .last() as RecordSearchModel).vessel.name == filteredRecords.last().vessel?.name
        )
        assert(
            (searchDataSource.applyFilter(filter)
                .last() as RecordSearchModel).vessel.permitNumber == filteredRecords.last().vessel?.permitNumber
        )
    }

    @Test
    fun testEmptyCrewDataSource() {
        val mockedReport = Report()
        val searchDataSource =
            complexSearchViewModel.getDataSource(ComplexSearchFragment.SearchCrew, mockedReport)

        assert(searchDataSource.initiateDataBlocking().size == 1)
        assert(searchDataSource.initiateDataBlocking().first() is AddSearchModel)
    }

    @Test
    fun testCrewDataSource() {
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

        assert(searchDataSource.initiateDataBlocking().size == initiateCrew.size + 1)
        assert(searchDataSource.initiateDataBlocking().first() is CrewSearchModel)
        assert(searchDataSource.initiateDataBlocking().last() is AddSearchModel)

        val filter = "3"
        val filteredCrew = listOf(
            CrewMember().apply {
                name = "Crew 3"
                license = "Crew license 3"
            }
        )
        assert(searchDataSource.applyFilter(filter).size == filteredCrew.size + 1)
        assert(searchDataSource.applyFilter(filter).first() is CrewSearchModel)
        assert(!(searchDataSource.applyFilter(filter).first() as CrewSearchModel).isCaptain)
        assert(
            (searchDataSource.applyFilter(filter)
                .first() as CrewSearchModel).value.name == filteredCrew.first().name
        )
        assert(
            (searchDataSource.applyFilter(filter)
                .first() as CrewSearchModel).value.license == filteredCrew.first().license
        )
        assert(searchDataSource.applyFilter(filter).last() is AddSearchModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidSearchType() {
        complexSearchViewModel.getDataSource(BaseSearchType(), null)
    }
}