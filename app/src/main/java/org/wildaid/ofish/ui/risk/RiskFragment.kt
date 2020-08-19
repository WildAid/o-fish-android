package org.wildaid.ofish.ui.risk

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.report.SafetyLevel
import org.wildaid.ofish.databinding.FragmentRiskBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard

class RiskFragment : BaseReportFragment(R.layout.fragment_risk) {
    private lateinit var viewDataBinding: FragmentRiskBinding
    private val fragmentViewModel: RiskViewModel by viewModels { getViewModelFactory() }
    private lateinit var buttonList: Map<String, View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initReport(currentReport)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentRiskBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = this@RiskFragment.viewLifecycleOwner
            buttonList = mapOf(
                SafetyColor.Green.name to btnGreen,
                SafetyColor.Amber.name to btnAmber,
                SafetyColor.Red.name to btnRed
            )
        }

        fragmentViewModel.riskLiveData.observe(viewLifecycleOwner, Observer {
            updateRisks(it)
        })

        fragmentViewModel.userEventsLiveData.observe(viewLifecycleOwner, EventObserver() {
            proceedNext()
        })
    }

    private fun updateRisks(risk: SafetyLevel) {
        buttonList.forEach { (k, v) ->
            v.isSelected = k == risk.level
        }
        viewDataBinding.noteEditLayout.hint = getString(R.string.reason_for_risk, risk.level)
    }

    private fun proceedNext() {
        hideKeyboard()
        onNextListener.onNextClicked()
    }
}