package org.wildaid.ofish.ui.basicinformation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.RealmList
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.util.LATITUDE
import org.wildaid.ofish.util.LONGITUDE
import org.wildaid.ofish.util.convert
import java.util.*

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

    fun onButtonClicked(id: Int) {
        buttonId.value = Event(id)
    }

    fun setLocation(lat: Double, long: Double) {
        currentReport.location = RealmList(long, lat)
        latitude.value = convert(lat, LATITUDE)
        longitude.value = convert(long, LONGITUDE)
    }

    fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        c.time = currentReport.date!!
        c.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        currentReport.date = c.time
        reportLiveData.value = currentReport
    }

    fun updateTime(hourOfDay: Int, minute: Int) {
        val c = Calendar.getInstance()
        c.time = currentReport.date!!
        c.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        currentReport.date = c.time
//        reportLiveData.value = currentReport
    }
}