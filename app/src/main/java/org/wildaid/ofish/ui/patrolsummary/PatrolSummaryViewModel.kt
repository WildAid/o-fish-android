package org.wildaid.ofish.ui.patrolsummary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import java.util.*

class PatrolSummaryViewModel(val repository: Repository) : ViewModel() {

    val buttonId = MutableLiveData<Event<Int>>()
    val dutyStartTime = MutableLiveData<Date>()
    val dutyEndTime = MutableLiveData<Date>(Date())
    val reports = MutableLiveData<List<Report>>()
    val errorMessage = MutableLiveData<Event<Int>>()

    init {
        reports.value = repository.findReportsForCurrentDuty()
        dutyStartTime.value = getRecentStartDateOrNewDate()
    }

    private fun getRecentStartDateOrNewDate() =
        repository.getRecentStartCurrentDuty()?.date ?: Date()

    fun onButtonClicked(id: Int) {
        buttonId.value = Event(id)
    }

    fun updateDate(year: Int, month: Int, dayOfMonth: Int, startTime: Boolean) {
        val newDate =
            if (startTime) {
                createDate(dutyStartTime, year, month, dayOfMonth)
            } else {
                createDate(dutyEndTime, year, month, dayOfMonth)
            }
        validateAndUpdateDateIfNeeded(newDate, startTime)
    }

    fun updateTime(hourOfDay: Int, minute: Int, startTime: Boolean) {
        val newDate =
            if (startTime) {
                createTime(dutyStartTime, hourOfDay, minute)
            } else {
                createTime(dutyEndTime, hourOfDay, minute)
            }
        validateAndUpdateDateIfNeeded(newDate, startTime)
    }

    private fun validateAndUpdateDateIfNeeded(newDate: Date, startTime: Boolean) {
        if (newDate.after(Date())) {
            errorMessage.value = Event(R.string.error_date_in_future)
            return
        }

        if (startTime) {
            if (newDate.before(dutyEndTime.value)) {
                repository.updateStartDateForCurrentDuty(newDate)
                dutyStartTime.value = getRecentStartDateOrNewDate()
                reports.value = repository.findReportsForCurrentDuty()
            } else {
                errorMessage.value = Event(R.string.error_start_date)
            }
        } else {
            if (newDate.after(dutyStartTime.value)) {
                dutyEndTime.value = newDate
            } else {
                errorMessage.value = Event(R.string.error_end_date)
            }
        }
    }

    private fun createDate(
        dutyTime: MutableLiveData<Date>,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): Date {
        val c = Calendar.getInstance()
        c.time = dutyTime.value!!
        c.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        return c.time
    }

    private fun createTime(dutyTime: MutableLiveData<Date>, hourOfDay: Int, minute: Int): Date {
        val c = Calendar.getInstance()
        c.time = dutyTime.value!!
        c.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        return c.time
    }
}