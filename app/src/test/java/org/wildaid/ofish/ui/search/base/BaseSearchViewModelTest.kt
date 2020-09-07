package org.wildaid.ofish.ui.search.base

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment
import org.wildaid.ofish.ui.search.simple.SimpleSearchFragment

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class BaseSearchViewModelTest {
    @MockK
    private lateinit var mockedRepository: Repository
    private lateinit var testViewModel: TestViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testViewModel = TestViewModel(mockedRepository)
    }

    @Test
    fun initDataList() {
        val requiredValue = TestViewModel(mockedRepository).TestSearchDataSource().initiateData()

        testViewModel.initDataList(BaseSearchType(), null)
        assert(testViewModel.dataList.value == requiredValue)
    }

    @Test
    fun applyFilter() {
        val requiredValue = TestViewModel(mockedRepository).TestSearchDataSource().applyFilter("U")

        testViewModel.initDataList(BaseSearchType(), null)
        testViewModel.applyFilter("U")

        assert(testViewModel.dataList.value == requiredValue)
    }

    @Test
    fun isComplexSearchFragmentSearchRecordsTest() {
        assert(testViewModel.isReportSearchEmpty())
        assert(testViewModel.dataList.value.isNullOrEmpty())

        testViewModel.initDataList(ComplexSearchFragment.SearchRecords, null)

        assert(!testViewModel.isReportSearchEmpty())
        assert(!testViewModel.dataList.value.isNullOrEmpty())

        assert(
            testViewModel.dataList.value == TestViewModel(mockedRepository).TestSearchDataSource()
                .initiateData()
        )
    }

    @Test
    fun isComplexSearchFragmentSearchBoardVesselsTest() {
        assert(testViewModel.isReportSearchEmpty())
        assert(testViewModel.dataList.value.isNullOrEmpty())

        testViewModel.initDataList(ComplexSearchFragment.SearchBoardVessels, null)

        assert(!testViewModel.isReportSearchEmpty())
        assert(!testViewModel.dataList.value.isNullOrEmpty())

        assert(
            testViewModel.dataList.value == TestViewModel(mockedRepository).TestSearchDataSource()
                .initiateData()
        )
    }

    @Test
    fun isComplexSearchFragmentDutyReportsTest() {
        assert(testViewModel.isReportSearchEmpty())
        assert(testViewModel.dataList.value.isNullOrEmpty())

        testViewModel.initDataList(ComplexSearchFragment.DutyReports, null)

        assert(!testViewModel.isReportSearchEmpty())
        assert(!testViewModel.dataList.value.isNullOrEmpty())

        assert(
            testViewModel.dataList.value == TestViewModel(mockedRepository).TestSearchDataSource()
                .initiateData()
        )
    }

    @Test
    fun testIsRecordSearch() {
        assert(testViewModel.isRecordSearch(ComplexSearchFragment.SearchRecords))
        assert(testViewModel.isRecordSearch(ComplexSearchFragment.SearchBoardVessels))
        assert(testViewModel.isRecordSearch(ComplexSearchFragment.DutyReports))

        assert(!testViewModel.isRecordSearch(ComplexSearchFragment.SearchBusiness))
        assert(!testViewModel.isRecordSearch(SimpleSearchFragment.SearchSpecies))
        assert(!testViewModel.isRecordSearch(BaseSearchType()))
    }

    @Test
    fun testProgress() {
        assert(testViewModel.progressLiveData.value == false)
        testViewModel.initDataList(BaseSearchType(), null)
        testViewModel.applyFilter("sdds")
        //Unable to test progress since it is blocking request
        assert(testViewModel.progressLiveData.value == false)
    }
}

class TestViewModel(mockedRepository: Repository) :
    BaseSearchViewModel<Any>(mockedRepository, ApplicationProvider.getApplicationContext()) {

    override fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource {
        return TestSearchDataSource()
    }

    inner class TestSearchDataSource : BaseSearchViewModel<Any>.SearchDataSource() {
        override fun initiateData(): List<Any> {
            return listOf("CA", "UA", "UK", "MX", "US", "DE")
        }

        override fun applyFilter(filter: String): List<Any> {
            return listOf("UA", "UK", "US")
        }
    }
}



