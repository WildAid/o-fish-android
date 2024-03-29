package org.wildaid.ofish.ui.basicinformation

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.realm.RealmList
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.MpaData
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.MpaViewModel
import org.wildaid.ofish.util.LATITUDE
import org.wildaid.ofish.util.LONGITUDE
import org.wildaid.ofish.util.convert
import java.util.*

class BasicInformationViewModel(val repository: Repository) : MpaViewModel() {

    val mpaObservable = ObservableBoolean(true)

    private val _mpaLiveData = MutableLiveData<Boolean>()
    val mpaLiveData: LiveData<Boolean>
        get() = _mpaLiveData

    private val _reportLiveData = MutableLiveData<Report>()
    val reportLiveData: LiveData<Report>
        get() = _reportLiveData

    private val _basicInfoUserEventLiveData = MutableLiveData<Event<BasicInfoUserEvent>>()
    val basicInfoUserEventLiveData: LiveData<Event<BasicInfoUserEvent>>
        get() = _basicInfoUserEventLiveData

    private val _latitude = MutableLiveData<String>()
    val latitude: LiveData<String>
        get() = _latitude

    private val _longitude = MutableLiveData<String>()
    val longitude: LiveData<String>
        get() = _longitude

    private lateinit var currentReport: Report

    fun initReport(report: Report) {
        currentReport = report
        _reportLiveData.value = report
    }

    fun next() {
        _basicInfoUserEventLiveData.value = Event(BasicInfoUserEvent.NextEvent)
    }

    fun chooseDate() {
        _basicInfoUserEventLiveData.value = Event(BasicInfoUserEvent.ChooseDate)

    }

    fun chooseTime() {
        _basicInfoUserEventLiveData.value = Event(BasicInfoUserEvent.ChooseTime)
    }

    fun setLocation(lat: Double, long: Double) {
        currentReport.location = RealmList(long, lat)
        _latitude.value = convert(lat, LATITUDE)
        _longitude.value = convert(long, LONGITUDE)
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
        _reportLiveData.value = currentReport
    }

    fun updateTime(hourOfDay: Int, minute: Int) {
        val c = Calendar.getInstance()
        c.time = currentReport.date!!
        c.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        currentReport.date = c.time
        _reportLiveData.value = currentReport
    }

    fun onMpaClick() {
        mpaObservable.set(!mpaObservable.get())
        _mpaLiveData.value = mpaObservable.get()
    }

    fun fetchMpaData(): MutableList<MpaData> {
        return super.fetchMpaData(repository.getAllProtectionMarineAreas())
    }

    sealed class BasicInfoUserEvent {
        object NextEvent : BasicInfoUserEvent()
        object ChooseDate : BasicInfoUserEvent()
        object ChooseTime : BasicInfoUserEvent()
    }
}