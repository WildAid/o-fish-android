package org.wildaid.ofish.ui.risk

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentRiskBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard

class RiskFragment : BaseReportFragment(R.layout.fragment_risk) {
    private lateinit var viewDataBinding: FragmentRiskBinding
    private val fragmentViewModel: RiskViewModel by viewModels { getViewModelFactory() }
    private lateinit var buttonList: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initReport(currentReport)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentRiskBinding.bind(view)
            .apply {
                this.viewmodel = fragmentViewModel
                this.lifecycleOwner = this@RiskFragment.viewLifecycleOwner
                buttonList = listOf(this.btnGreen, this.btnAmber, this.btnRed)
            }

        fragmentViewModel.selectedButtonLiveData.observe(
            viewLifecycleOwner,
            Observer { selectButton(it) })

        fragmentViewModel.buttonId.observe(
            viewLifecycleOwner, EventObserver(::onButtonClicked)
        )
    }

    private fun selectButton(id: Int) {
        buttonList.forEach {
            it.isSelected = it.id == id
        }
        viewDataBinding.noteEditLayout.hint = getString(R.string.reason_for_risk, fragmentViewModel.getRiskColor())
    }

    private fun onButtonClicked(buttonId: Int) {
        hideKeyboard()
        when (buttonId) {
            R.id.btn_next -> {
                onNextListener.onNextClicked()
            }
        }
    }
}
