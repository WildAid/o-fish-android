package org.wildaid.ofish.ui.search.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.complex.AddSearchModel
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment

abstract class BaseSearchViewModel<T>(
    protected val repository: Repository, application: Application
) : AndroidViewModel(application) {

    protected var _dataList = MutableLiveData<List<T>>()
    val dataList: LiveData<List<T>>
        get() = _dataList

    protected var _progressLiveData = MutableLiveData(false)
    val progressLiveData: LiveData<Boolean>
        get() = _progressLiveData

    private lateinit var searchDataSource: SearchDataSource

    abstract fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource

    fun initDataList(searchEntity: BaseSearchType, report: Report?) {
        searchDataSource = getDataSource(searchEntity, report)
        _dataList.value = searchDataSource.initiateData()
    }

    fun applyFilter(filter: String) {
        _progressLiveData.value = true
        _dataList.value = searchDataSource.applyFilter(filter)
        _progressLiveData.value = false
    }

    fun isReportSearchEmpty() = isDataEmpty()

    fun isRecordSearch(searchEntity: BaseSearchType) = when (searchEntity) {
        is ComplexSearchFragment.SearchRecords -> true
        is ComplexSearchFragment.SearchBoardVessels -> true
        is ComplexSearchFragment.DutyReports -> true
        else -> false
    }

    private fun isDataEmpty(): Boolean {
        val value = dataList.value
        if (value.isNullOrEmpty()) return true
        if (value.size == 1 && value[0] is AddSearchModel) return true
        return false
    }

    abstract inner class SearchDataSource {
        abstract fun initiateData(): List<T>
        abstract fun applyFilter(filter: String): List<T>
    }
}