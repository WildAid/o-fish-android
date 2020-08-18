package org.wildaid.ofish.ui.search.simple

import androidx.fragment.app.viewModels
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.base.BaseSearchType
import org.wildaid.ofish.util.getViewModelFactory

class SimpleSearchFragment : BaseSearchFragment<String>() {
    override fun createViewModel() =
        viewModels<SimpleSearchViewModel> { getViewModelFactory() }.value

    override fun createAdapter(itemListener: (String) -> Unit) = SimpleSearchAdapter(itemListener)

    override fun getSearchTitle(): String {
        return getString(
            when (currentSearchEntity) {
                is SearchFlagState -> R.string.flag_state_title
                is SearchEms -> R.string.electronic_monitoring_system
                is SearchFishery -> R.string.fishery
                is SearchGear -> R.string.gear
                is SearchActivity -> R.string.activity
                is SearchSpecies -> R.string.species
                else -> R.string.empty
            }
        )
    }

    override fun getSearchHint() = getSearchTitle()

    object SearchFlagState : BaseSearchType()
    object SearchEms : BaseSearchType()
    object SearchFishery : BaseSearchType()
    object SearchGear : BaseSearchType()
    object SearchActivity : BaseSearchType()
    object SearchSpecies : BaseSearchType()
    object SearchAmount : BaseSearchType()
}