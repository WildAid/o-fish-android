package org.wildaid.ofish.ui.basicinformation

import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.util.LATITUDE
import org.wildaid.ofish.util.LONGITUDE
import org.wildaid.ofish.util.convert
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class BasicInformationViewModelTest {

    @MockK
    private lateinit var basicViewModelTest: BasicInformationViewModel

    private val report = Report()
    private val latitude = 54.594
    private val longitude = 177.16
    private val year = 2002
    private val month = 4
    private val day = 25
    private val hourOfDay = 19
    private val minute = 45
    private val buttonId = 1045

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
        basicViewModelTest.onButtonClicked(buttonId)
        assert(basicViewModelTest.buttonId.value?.peekContent() == buttonId)
    }

    @Test
    fun setLocation() {
        basicViewModelTest.initReport(report)

        assertNull(basicViewModelTest.latitude.value)
        assertNull(basicViewModelTest.longitude.value)

        basicViewModelTest.setLocation(latitude, longitude)

        val convertedLatitude = convert(latitude, LATITUDE)
        val convertedLongitude = convert(longitude, LONGITUDE)

        assert(basicViewModelTest.latitude.value == convertedLatitude)
        assert(basicViewModelTest.longitude.value == convertedLongitude)
    }

    @Test
    fun updateDateTest() {
        basicViewModelTest.initReport(report)
        basicViewModelTest.updateDate(year, month, day)

        val c = Calendar.getInstance()
        c.time = report.date

        c.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        report.date = c.time

        assert(basicViewModelTest.reportLiveData.value?.date == c.time)
    }

    @Test
    fun updateTime() {
        basicViewModelTest.initReport(report)
        basicViewModelTest.updateTime(hourOfDay, minute)

        val c = Calendar.getInstance()
        c.time = report.date!!

        c.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        report.date = c.time

        assert(basicViewModelTest.reportLiveData.value?.date == c.time)
    }
}