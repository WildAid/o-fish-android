package org.wildaid.ofish.ui.basicinformation

import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.util.LATITUDE
import org.wildaid.ofish.util.LONGITUDE
import org.wildaid.ofish.util.convert

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class BasicInformationViewModelTest {

    @MockK
    private lateinit var basicViewModelTest: BasicInformationViewModel

    private val report = Report()
    private val latitude = 54.594
    private val longitude = 177.16

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        basicViewModelTest = BasicInformationViewModel()
    }

    @Test
    fun initReportTest() {
        basicViewModelTest.initReport(report)
        assert(basicViewModelTest.reportLiveData.value == report)
    }

    @Test
    fun onButtonClickedTest() {
        basicViewModelTest.onButtonClicked(1045)
        assert(basicViewModelTest.buttonId.value?.peekContent() == 1045)
    }

    @Test
    fun setLocation() {
        basicViewModelTest.initReport(report)

        basicViewModelTest.setLocation(latitude, longitude)

        basicViewModelTest.latitude.value = convert(latitude, LATITUDE)
        basicViewModelTest.longitude.value = convert(longitude, LONGITUDE)
    }

    @Test
    fun updateDateTest() {
        basicViewModelTest.initReport(report)

        basicViewModelTest.updateDate(2002, 25, 4)
        basicViewModelTest.reportLiveData.value = report

        assert(basicViewModelTest.reportLiveData.value == report)
    }

    @Test
    fun updateTime() {
        basicViewModelTest.initReport(report)

        basicViewModelTest.updateTime(19, 45)

        basicViewModelTest.reportLiveData.value = report
        assert(basicViewModelTest.reportLiveData.value == report)
    }
}