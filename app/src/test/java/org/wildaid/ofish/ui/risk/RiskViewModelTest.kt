package org.wildaid.ofish.ui.risk

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockkClass
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.report.Inspection
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.data.report.SafetyLevel
import org.wildaid.ofish.data.report.Summary

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class RiskViewModelTest {

    private lateinit var riskVM: RiskViewModel

    @MockK
    private lateinit var report: Report
    @MockK(relaxed = true)
    private lateinit var repository: Repository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        report.apply {
            inspection = mockkClass(Inspection::class).apply {
                summary = mockkClass(Summary::class).apply {
                    safetyLevel = SafetyLevel().apply {
                        level = SafetyColor.Green.name
                    }
                }
            }
        }

        riskVM = RiskViewModel(repository, ApplicationProvider.getApplicationContext()).apply {
            initViewModel(report, mutableListOf())
        }
    }

    @Test
    fun testInitReport() {
        assert(riskVM.userEventsLiveData.value == null)

        assert(riskVM.riskLiveData.value != null)
        assert(riskVM.riskLiveData.value?.safetyLevel?.level == SafetyColor.Green.name)
        assert(riskVM.riskLiveData.value?.safetyLevel?.amberReason?.isBlank() == true)
    }

    @Test
    fun testGreenChosen() {
        riskVM.onGreenChosen()

        assert(riskVM.userEventsLiveData.value == null)

        assert(riskVM.riskLiveData.value != null)
        assert(riskVM.riskLiveData.value?.safetyLevel?.level == SafetyColor.Green.name)
        assert(riskVM.riskLiveData.value?.safetyLevel?.amberReason?.isBlank() == true)
    }

    @Test
    fun testAmberChosen() {
        riskVM.onAmberChosen()

        assert(riskVM.userEventsLiveData.value == null)

        assert(riskVM.riskLiveData.value != null)
        assert(riskVM.riskLiveData.value?.safetyLevel?.level == SafetyColor.Amber.name)
        assert(riskVM.riskLiveData.value?.safetyLevel?.amberReason?.isBlank() == true)
    }

    @Test
    fun testRedChosen() {
        riskVM.onRedChosen()

        assert(riskVM.userEventsLiveData.value == null)

        assert(riskVM.riskLiveData.value != null)
        assert(riskVM.riskLiveData.value?.safetyLevel?.level == SafetyColor.Red.name)
        assert(riskVM.riskLiveData.value?.safetyLevel?.amberReason?.isBlank() == true)
    }

    @Test
    fun testNextClicked() {
        assert(riskVM.userEventsLiveData.value == null)
        riskVM.onNextClicked()
        assert(riskVM.userEventsLiveData.value?.peekContent() == RiskViewModel.RiskUserEvent.NextEvent)
    }
}