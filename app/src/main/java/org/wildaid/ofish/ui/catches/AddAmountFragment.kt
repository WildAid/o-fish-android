package org.wildaid.ofish.ui.catches

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_add_amount.*
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.createreport.CreateReportViewModel
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.simple.SimpleSearchFragment
import org.wildaid.ofish.util.getViewModelFactory


class AddAmountFragment : Fragment(R.layout.fragment_add_amount) {
    private val amountAdapter: AddAmountAdapter by lazy { createAdapter() }
    private val navigation: NavController by lazy { findNavController() }
    private val activityViewModel: CreateReportViewModel by activityViewModels { getViewModelFactory() }

    override fun onStart() {
        activityViewModel.isOnSearch = true
        super.onStart()
    }

    override fun onStop() {
        activityViewModel.isOnSearch = false
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        amount_recycler.apply {
            adapter = amountAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }

        add_amount_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey)
        add_amount_toolbar.setNavigationOnClickListener {
            navigation.popBackStack()
        }
    }

    private fun createAdapter(): AddAmountAdapter =
        AddAmountAdapter(
            resources.getStringArray(R.array.amount).toList(),
            onItemClicked = { onItemSelected(it) })

    private fun onItemSelected(item: String) {
        navigation.previousBackStackEntry?.let {
            it.savedStateHandle.set(BaseSearchFragment.SEARCH_RESULT, item)
            it.savedStateHandle.set(
                BaseSearchFragment.SEARCH_ENTITY_KEY,
                SimpleSearchFragment.SearchAmount
            )
        }
        navigation.popBackStack()
    }
}

