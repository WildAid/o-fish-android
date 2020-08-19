package org.wildaid.ofish.ui.risk

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.data.report.SafetyLevel

class RiskViewModel(application: Application) : AndroidViewModel(application) {
    val riskLiveData = MutableLiveData<SafetyLevel>()
    val userEventsLiveData = MutableLiveData<Event<RiskUserEvent>>()

    lateinit var report: Report
    lateinit var safetyLevel: SafetyLevel

    fun initReport(currentReport: Report) {
        report = currentReport
        report.inspection?.summary?.safetyLevel?.let {
            safetyLevel = it.apply { level = SafetyColor.Green.name }
            riskLiveData.value = safetyLevel
        }
    }

    fun onGreenChosen() {
        safetyLevel.level = SafetyColor.Green.name
        riskLiveData.value = safetyLevel
    }

    fun onAmberChosen() {
        safetyLevel.level = SafetyColor.Amber.name
        riskLiveData.value = safetyLevel
    }

    fun onRedChosen() {
        safetyLevel.level = SafetyColor.Red.name
        riskLiveData.value = safetyLevel
    }

    fun onNextClicked() {
        userEventsLiveData.value = Event(RiskUserEvent.NextEvent)
    }

    sealed class RiskUserEvent {
        object NextEvent : RiskUserEvent()
    }
}