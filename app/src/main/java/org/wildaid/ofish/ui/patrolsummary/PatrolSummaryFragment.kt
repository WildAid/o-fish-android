package org.wildaid.ofish.ui.patrolsummary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_patrol_summary.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.app.ServiceLocator
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.databinding.FragmentPatrolSummaryBinding
import org.wildaid.ofish.ui.base.CARDS_OFFSET_SIZE
import org.wildaid.ofish.ui.crew.VerticalSpaceItemDecoration
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.ui.reportdetail.ADDITIONAL_TITLE
import org.wildaid.ofish.ui.reportdetail.BOARD_VESSEL_ALLOWED
import org.wildaid.ofish.ui.reportdetail.KEY_REPORT_ID
import org.wildaid.ofish.util.getViewModelFactory
import java.util.*

class PatrolSummaryFragment : Fragment(R.layout.fragment_patrol_summary) {

    private val fragmentViewModel: PatrolSummaryViewModel by viewModels { getViewModelFactory() }
    private val activityViewModel: HomeActivityViewModel by activityViewModels { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentPatrolSummaryBinding
    private val summaryAdapter: PatrolSummaryAdapter by lazy { createAdapter() }
    private val navigation: NavController by lazy { findNavController() }
    private var isStartTime: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(patrol_summary_toolbar)
        patrol_summary_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)

        viewDataBinding = FragmentPatrolSummaryBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = this@PatrolSummaryFragment.viewLifecycleOwner
        }

        summary_recycler.apply {
            adapter = summaryAdapter
            addItemDecoration(VerticalSpaceItemDecoration(CARDS_OFFSET_SIZE))
        }

        fragmentViewModel.patrolSummaryUserEventLiveData.observe(
            viewLifecycleOwner, EventObserver(::handleUserEvent)
        )

        fragmentViewModel.reports.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            summaryAdapter.setItems(it)
        })

        fragmentViewModel.errorMessage.observe(viewLifecycleOwner, EventObserver(::showError))
    }

    private fun showError(@StringRes stringId: Int) {
        Snackbar.make(requireView(), stringId, Snackbar.LENGTH_LONG).show()
    }

    private fun createAdapter(): PatrolSummaryAdapter {
        return PatrolSummaryAdapter(
            ServiceLocator.provideRepository(requireContext()),
            ::itemListener
        )
    }

    private fun handleUserEvent(event: PatrolSummaryViewModel.PatrolSummaryUserEvent) {
        when (event) {
            PatrolSummaryViewModel.PatrolSummaryUserEvent.ChangeStartDateEvent -> {
                isStartTime = true
                peekDate()
            }
            PatrolSummaryViewModel.PatrolSummaryUserEvent.ChangeStartTimeEvent -> {
                isStartTime = true
                peekTime()
            }
            PatrolSummaryViewModel.PatrolSummaryUserEvent.ChangeEndDateEvent -> {
                isStartTime = false
                peekDate()
            }
            PatrolSummaryViewModel.PatrolSummaryUserEvent.ChangeEndTimeEvent -> {
                isStartTime = false
                peekTime()
            }
            PatrolSummaryViewModel.PatrolSummaryUserEvent.GoOffDutyEvent -> goOffDuty()
        }
    }

    private fun goOffDuty() {
        activityViewModel.onDutyChanged(false, fragmentViewModel.dutyEndTime.value!!)
        navigation.navigate(R.id.action_patrolSummaryFragment_to_home_fragment)
    }

    private fun peekDate() {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val dialog = DatePickerDialog(
            requireContext(), ::onDatePicked,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun onDatePicked(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        fragmentViewModel.updateDate(year, month, dayOfMonth, isStartTime)
    }

    private fun peekTime() {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val dialog = TimePickerDialog(
            requireContext(),
            ::onTimePicked,
            calendar.get(Calendar.HOUR),
            calendar.get(Calendar.MINUTE),
            false
        )
        dialog.show()
    }

    private fun onTimePicked(timePicker: TimePicker, hourOfDay: Int, minute: Int) {
        fragmentViewModel.updateTime(hourOfDay, minute, isStartTime)
    }

    private fun itemListener(report: Report) {
        val navigationArgs = bundleOf(
            KEY_REPORT_ID to report._id,
            BOARD_VESSEL_ALLOWED to false,
            ADDITIONAL_TITLE to getString(R.string.back)
        )
        navigation.navigate(
            R.id.action_patrolSummaryFragment_to_report_details_fragment,
            navigationArgs
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) navigation.popBackStack()
        return super.onOptionsItemSelected(item)
    }
}