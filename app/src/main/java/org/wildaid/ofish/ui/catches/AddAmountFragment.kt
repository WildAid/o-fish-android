package org.wildaid.ofish.ui.catches

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_add_amount.*
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.simple.SimpleSearchFragment

class AddAmountFragment : Fragment(R.layout.fragment_add_amount) {
    private val amountAdapter: AddAmountAdapter by lazy { createAdapter() }
    private val navigation: NavController by lazy { findNavController() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        amount_recycler.apply {
            adapter = amountAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun createAdapter(): AddAmountAdapter =
        AddAmountAdapter(
            resources.getStringArray(R.array.amount).toList(),
            onItemClicked = { onItemSelected(it) })

    private fun onItemSelected(item: String) {
        navigation.previousBackStackEntry?.let {
            it.savedStateHandle.set(BaseSearchFragment.SEARCH_RESULT, item)
            it.savedStateHandle.set(BaseSearchFragment.SEARCH_ENTITY_KEY, SimpleSearchFragment.SearchAmount)
        }
        navigation.popBackStack()
    }

}

