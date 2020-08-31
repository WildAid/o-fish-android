package org.wildaid.ofish.ui.search.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.complex.AddSearchModel
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment

abstract class BaseSearchViewModel<T>(
    protected val repository: Repository, application: Application
) : AndroidViewModel(application) {

    val dataList = MutableLiveData<List<T>>()
    val progressLiveData = MutableLiveData(false)

    private lateinit var searchDataSource: SearchDataSource

    abstract fun getDataSource(searchEntity: BaseSearchType, report: Report?): SearchDataSource

    fun initDataList(searchEntity: BaseSearchType, report: Report?) {
        searchDataSource = getDataSource(searchEntity, report)
        dataList.value = searchDataSource.initiateData()
    }

    fun applyFilter(filter: String) {
        progressLiveData.value = true
        dataList.value = searchDataSource.applyFilter(filter)
        progressLiveData.value = false
    }

    fun isReportSearchEmpty(searchEntity: BaseSearchType): Boolean {
        return isRecordSearch(searchEntity) && isDataEmpty()
    }

    private fun isRecordSearch(searchEntity: BaseSearchType) = when (searchEntity) {
        is ComplexSearchFragment.SearchRecords -> true
        is ComplexSearchFragment.SearchBoardVessels -> true
        is ComplexSearchFragment.DutyReports -> true
        else -> throw IllegalArgumentException("Unknown searchEntity -> $searchEntity")
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