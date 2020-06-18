package org.wildaid.ofish.ui.basicinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.util.LATITUDE
import org.wildaid.ofish.util.LONGITUDE
import org.wildaid.ofish.util.convert

class BasicInformationViewModel : ViewModel() {
    val reportLiveData = MutableLiveData<Report>()
    val buttonId = MutableLiveData<Event<Int>>()
    val latitude = MutableLiveData<String>()
    val longitude = MutableLiveData<String>()

    private lateinit var currentReport: Report

    fun initReport(report: Report) {
        currentReport = report
        reportLiveData.value = report
    }

    fun onNextClicked() {
        buttonId.value = Event(R.id.btn_next)
    }

    fun setLocation(lat: Double, long: Double) {
        currentReport.location?.latitude = lat
        currentReport.location?.longitude = long
        latitude.value = convert(lat, LATITUDE)
        longitude.value = convert(long, LONGITUDE)
    }
}
