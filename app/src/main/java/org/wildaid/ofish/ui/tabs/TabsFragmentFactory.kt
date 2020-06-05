package org.wildaid.ofish.ui.tabs

import android.content.Context
import com.google.android.material.tabs.TabLayoutMediator
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.activity.ActivitiesFragment
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.ui.base.OnNextClickedListener
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.ui.basicinformation.BasicInformationFragment
import org.wildaid.ofish.ui.catches.CatchFragment
import org.wildaid.ofish.ui.crew.CrewFragment
import org.wildaid.ofish.ui.notes.NotesFragment
import org.wildaid.ofish.ui.risk.RiskFragment
import org.wildaid.ofish.ui.vessel.VesselFragment
import org.wildaid.ofish.ui.violation.ViolationFragment


class TabsFragmentFactory(
    private val context: Context,
    private val listener: OnNextClickedListener,
    private val report: Report,
    private val reportPhotos: MutableList<PhotoItem>
) {
    val fragmentTitleConfigurator = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
        tab.setCustomView(R.layout.item_custom_tab)
        when (position) {
            0 -> tab.text = context.getText(R.string.basic_information)
            1 -> tab.text = context.getText(R.string.vessel)
            2 -> tab.text = context.getText(R.string.crew)
            3 -> tab.text = context.getText(R.string.activity)
            4 -> tab.text = context.getText(R.string.catch_title)
            5 -> tab.text = context.getText(R.string.violation)
            6 -> tab.text = context.getString(R.string.risk)
            7 -> tab.text = context.getText(R.string.notes)
            else -> throw IllegalArgumentException("Not supported fragment number.")
        }
    }

    fun createFragmentForPosition(position: Int): BaseReportFragment {
        val fragment = when (position) {
            0 -> BasicInformationFragment()
            1 -> VesselFragment()
            2 -> CrewFragment()
            3 -> ActivitiesFragment()
            4 -> CatchFragment()
            5 -> ViolationFragment()
            6 -> RiskFragment()
            7 -> NotesFragment()
            else -> throw IllegalArgumentException("Not supported fragment number.")
        }
        fragment.onNextListener = listener
        fragment.currentReport = report
        fragment.currentReportPhotos = reportPhotos
        return fragment
    }
}