package org.wildaid.ofish.ui.search.base

import android.os.Build
import android.os.CountDownTimer
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment
import org.wildaid.ofish.ui.search.simple.SimpleSearchFragment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
    fun testInitDataList() {
        runBlockingTest {
            testViewModel.initDataList(BaseSearchType(), null)
            val result = TestViewModel(mockedRepository).TestSearchDataSource()
                .initiateData()
                .first()

            assert(testViewModel.dataList.value == result)
        }
    }

    @Test
    fun testIsSearchEmpty() {
        assert(testViewModel.isReportSearchEmpty())
        assert(testViewModel.dataList.value.isNullOrEmpty())

        testViewModel.initDataList(BaseSearchType(), null)

        assert(!testViewModel.isReportSearchEmpty())
        assert(!testViewModel.dataList.value.isNullOrEmpty())

        runBlockingTest {
            val result = TestViewModel(mockedRepository).TestSearchDataSource()
                .initiateData()
                .first()
            assert(testViewModel.dataList.value == result)
        }
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
    BaseSearchViewModel<Any>(ApplicationProvider.getApplicationContext()) {

    override fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource {
        return TestSearchDataSource()
    }

    inner class TestSearchDataSource : BaseSearchViewModel<Any>.SearchDataSource() {
        override fun initiateData(): Flow<List<Any>> {
            return flowOf(listOf("CA", "UA", "UK", "MX", "US", "DE"))
        }

        override fun applyFilter(filter: String): Flow<List<Any>> {
            return flowOf(listOf("UA", "UK", "US"))
        }
    }
}



