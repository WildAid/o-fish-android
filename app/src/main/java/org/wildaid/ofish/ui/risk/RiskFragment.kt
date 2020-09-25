package org.wildaid.ofish.ui.risk

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.databinding.FragmentRiskBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard

class RiskFragment : BaseReportFragment(R.layout.fragment_risk) {
    private lateinit var fragmentDataBinding: FragmentRiskBinding
    private val fragmentViewModel: RiskViewModel by viewModels { getViewModelFactory() }
    private lateinit var buttonList: Map<String, View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initReport(currentReport, currentReportPhotos)
    }

    override fun isAllRequiredFieldsNotEmpty(): Boolean {
        return true
    }

    override fun validateForms(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentDataBinding = FragmentRiskBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = this@RiskFragment.viewLifecycleOwner
            buttonList = mapOf(
                SafetyColor.Green.name to btnGreen,
                SafetyColor.Amber.name to btnAmber,
                SafetyColor.Red.name to btnRed
            )
        }

        fragmentDataBinding.riskEditPhotos.onPhotoRemoveListener =
            fragmentViewModel::removePhotoFromActivity

        fragmentDataBinding.riskEditPhotos.onPhotoClickListener = ::showFullImage

        fragmentViewModel.riskLiveData.observe(viewLifecycleOwner, Observer {
            updateRisks(it)
        })

        fragmentViewModel.userEventsLiveData.observe(viewLifecycleOwner, EventObserver() {
            when (it) {
                RiskViewModel.RiskUserEvent.AddAttachment -> peekImage { imageUri ->
                    fragmentViewModel.addPhotoAttachment(imageUri)
                }
                RiskViewModel.RiskUserEvent.NextEvent -> proceedNext()
            }
        })
    }

    private fun updateRisks(riskItem: RiskItem) {
        buttonList.forEach { (k, v) ->
            v.isSelected = k == riskItem.safetyLevel.level
        }
        fragmentDataBinding.noteEditLayout.hint =
            getString(R.string.reason_for_risk, riskItem.safetyLevel.level)
    }

    private fun proceedNext() {
        hideKeyboard()
        onNextListener.onNextClicked()
    }
}