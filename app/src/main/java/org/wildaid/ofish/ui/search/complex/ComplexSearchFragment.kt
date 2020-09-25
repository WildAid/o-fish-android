package org.wildaid.ofish.ui.search.complex

import androidx.fragment.app.viewModels
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.base.BaseSearchType
import org.wildaid.ofish.util.getViewModelFactory

class ComplexSearchFragment : BaseSearchFragment<SearchModel>() {
    override fun createViewModel() =
        viewModels<ComplexSearchViewModel> { getViewModelFactory() }.value

    override fun createAdapter(itemListener: (SearchModel) -> Unit) =
        ComplexSearchAdapter(itemListener)

    override fun getSearchTitle(): String {
        return getString(
            when (currentSearchEntity) {
                is SearchBusiness -> R.string.business
                is SearchViolation -> R.string.violation
                is SearchRecords -> R.string.find_records
                is SearchDrafts -> R.string.draft_boardings
                is SearchBoardVessels -> R.string.new_boarding
                is SearchCrew -> R.string.crew
                is DutyReports -> R.string.duty_report
                else -> R.string.empty
            }
        )
    }

    override fun getSearchHint(): String {
        return getString(
            when (currentSearchEntity) {
                is SearchBusiness -> R.string.business
                is SearchViolation -> R.string.violation
                is SearchRecords -> R.string.find_records
                is SearchDrafts -> R.string.draft_boardings
                is SearchBoardVessels -> R.string.find_records
                is DutyReports -> R.string.duty_report
                is SearchCrew -> R.string.crew
                else -> R.string.empty
            }
        )
    }

    object SearchBusiness : BaseSearchType()
    object SearchRecords : BaseSearchType()
    object SearchBoardVessels : BaseSearchType()
    object SearchViolation : BaseSearchType()
    object SearchCrew : BaseSearchType()
    object SearchDrafts : BaseSearchType()
    object DutyReports : BaseSearchType()
}