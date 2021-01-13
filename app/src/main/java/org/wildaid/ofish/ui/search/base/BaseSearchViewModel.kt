package org.wildaid.ofish.ui.search.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.complex.AddSearchModel
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment

abstract class BaseSearchViewModel<T>(application: Application) : AndroidViewModel(application) {

    private val _dataList = MutableLiveData<List<T>>()
    val dataList: LiveData<List<T>>
        get() = _dataList

    private val _progressLiveData = MutableLiveData(false)
    val progressLiveData: LiveData<Boolean>
        get() = _progressLiveData

    private lateinit var searchDataSource: SearchDataSource

    abstract fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource

    fun initDataList(searchEntity: BaseSearchType, report: Report?) {
        searchDataSource = getDataSource(searchEntity, report)
        searchDataSource.initiateData()
            .onEach { list -> _dataList.postValue(list) }
            .launchIn(viewModelScope)
    }

    fun applyFilter(filter: String) {
        searchDataSource.applyFilter(filter)
            .onStart { _progressLiveData.postValue(true) }
            .onEach { list ->
                _dataList.postValue(list)
                _progressLiveData.postValue(false)
            }
            .launchIn(viewModelScope)
    }

    fun isReportSearchEmpty() = isDataEmpty()

    fun isRecordSearch(searchEntity: BaseSearchType) = when (searchEntity) {
        is ComplexSearchFragment.SearchRecords -> true
        is ComplexSearchFragment.SearchBoardVessels -> true
        is ComplexSearchFragment.DutyReports -> true
        is ComplexSearchFragment.SearchDrafts -> true
        else -> false
    }

    private fun isDataEmpty(): Boolean {
        val value = dataList.value
        if (value.isNullOrEmpty()) return true
        if (value.size == 1 && value[0] is AddSearchModel) return true
        return false
    }

    abstract inner class SearchDataSource {
        abstract fun initiateData(): Flow<List<T>>
        abstract fun applyFilter(filter: String): Flow<List<T>>
    }
}