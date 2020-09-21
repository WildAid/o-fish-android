package org.wildaid.ofish.ui.patrolsummary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import java.util.*

class PatrolSummaryViewModel(val repository: Repository) : ViewModel() {

    private var _patrolSummaryUserEventLiveData = MutableLiveData<Event<PatrolSummaryUserEvent>>()
    val patrolSummaryUserEventLiveData: LiveData<Event<PatrolSummaryUserEvent>>
        get() = _patrolSummaryUserEventLiveData

    private var _dutyStartTime = MutableLiveData<Date>()
    val dutyStartTime: LiveData<Date>
        get() = _dutyStartTime

    private var _dutyEndTime = MutableLiveData<Date>(Date())
    val dutyEndTime: LiveData<Date>
        get() = _dutyEndTime

    private var _reports = MutableLiveData<List<Report>>()
    val reports: LiveData<List<Report>>
        get() = _reports

    private var _errorMessage = MutableLiveData<Event<Int>>()
    val errorMessage: LiveData<Event<Int>>
        get() = _errorMessage

    init {
        _reports.value = repository.findReportsForCurrentDuty()
        _dutyStartTime.value = getRecentStartDateOrNewDate()
    }

    private fun getRecentStartDateOrNewDate() =
        repository.getRecentStartCurrentDuty()?.date ?: Date()


    fun updateDate(year: Int, month: Int, dayOfMonth: Int, startTime: Boolean) {
        val newDate =
            if (startTime) {
                createDate(_dutyStartTime, year, month, dayOfMonth)
            } else {
                createDate(_dutyEndTime, year, month, dayOfMonth)
            }
        validateAndUpdateDateIfNeeded(newDate, startTime)
    }

    fun updateTime(hourOfDay: Int, minute: Int, startTime: Boolean) {
        val newDate =
            if (startTime) {
                createTime(_dutyStartTime, hourOfDay, minute)
            } else {
                createTime(_dutyEndTime, hourOfDay, minute)
            }
        validateAndUpdateDateIfNeeded(newDate, startTime)
    }

    private fun validateAndUpdateDateIfNeeded(newDate: Date, startTime: Boolean) {
        if (newDate.after(Date())) {
            _errorMessage.value = Event(R.string.error_date_in_future)
            return
        }

        if (startTime) {
            if (newDate.before(dutyEndTime.value)) {
                repository.updateStartDateForCurrentDuty(newDate)
                _dutyStartTime.value = getRecentStartDateOrNewDate()
                _reports.value = repository.findReportsForCurrentDuty()
            } else {
                _errorMessage.value = Event(R.string.error_start_date)
            }
        } else {
            if (newDate.after(dutyStartTime.value)) {
                _dutyEndTime.value = newDate
            } else {
                _errorMessage.value = Event(R.string.error_end_date)
            }
        }
    }

    fun changeStartDate() {
        _patrolSummaryUserEventLiveData.value = Event(PatrolSummaryUserEvent.ChangeStartDateEvent)
    }

    fun changeStartTime() {
        _patrolSummaryUserEventLiveData.value = Event(PatrolSummaryUserEvent.ChangeStartTimeEvent)
    }

    fun changeEndDate() {
        _patrolSummaryUserEventLiveData.value = Event(PatrolSummaryUserEvent.ChangeEndDateEvent)
    }

    fun changeEndTime() {
        _patrolSummaryUserEventLiveData.value = Event(PatrolSummaryUserEvent.ChangeEndTimeEvent)
    }

    fun goOffDuty() {
        _patrolSummaryUserEventLiveData.value = Event(PatrolSummaryUserEvent.GoOffDutyEvent)
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

    sealed class PatrolSummaryUserEvent {
        object ChangeStartDateEvent : PatrolSummaryUserEvent()
        object ChangeStartTimeEvent : PatrolSummaryUserEvent()
        object ChangeEndDateEvent : PatrolSummaryUserEvent()
        object ChangeEndTimeEvent : PatrolSummaryUserEvent()
        object GoOffDutyEvent : PatrolSummaryUserEvent()
    }
}