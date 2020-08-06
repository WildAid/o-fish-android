package org.wildaid.ofish.ui.risk

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.report.Report

class RiskViewModel(application: Application) : AndroidViewModel(application) {

    val noteVisibility = MutableLiveData(View.GONE)
    val selectedButtonLiveData = MutableLiveData<Int>()
    val buttonId = MutableLiveData<Event<Int>>()
    lateinit var report: Report
    var note = ""

    fun initReport(currentReport: Report) {
        report = currentReport
        report.inspection?.summary?.safetyLevel?.level = SafetyColor.Green.name
        selectedButtonLiveData.value = R.id.btn_green
    }

    fun onActionClicked() {
        report.inspection?.summary?.safetyLevel?.level?.let {
            if (it.isNotBlank()) {
                noteVisibility.value = View.VISIBLE
            }
        }
    }

    fun onButtonClicked(id: Int) {
        report.inspection?.summary?.safetyLevel?.level = when (id) {
            R.id.btn_green -> SafetyColor.Green.name
            R.id.btn_amber -> SafetyColor.Amber.name
            R.id.btn_red -> SafetyColor.Red.name
            else -> ""
        }

        selectedButtonLiveData.value = id
    }

    fun onNextClicked() {
        buttonId.value = Event(R.id.btn_next)
    }

    fun getRiskColor() = report.inspection?.summary?.safetyLevel?.level
}