package org.wildaid.ofish.ui.createreport

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.findNavController
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.base.ConfirmationDialogFragment
import org.wildaid.ofish.ui.base.DIALOG_CLICK_EVENT
import org.wildaid.ofish.ui.base.DialogButton
import org.wildaid.ofish.ui.base.DialogClickEvent
import org.wildaid.ofish.ui.home.KEY_CREATE_REPORT_RESULT
import org.wildaid.ofish.util.getViewModelFactory

const val KEY_CREATE_REPORT_ARGS = "bundle_of_info"
private const val DISCARD_DIALOG_ID = 17

class CreateReportActivity : AppCompatActivity() {
    private val activityViewModel: CreateReportViewModel by viewModels { getViewModelFactory() }
    private lateinit var navigation: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_report)
        navigation = findNavController(R.id.create_report_host_fragment).apply {
            setGraph(R.navigation.create_report_navigation, intent.extras)
        }

        activityViewModel.initReport()
        activityViewModel.discardReportLiveData.observe(this, EventObserver {
            showDiscardReportDialog()
        })

        subscribeToDialogEvents()
    }

    private fun subscribeToDialogEvents() {
        val navStack = navigation.currentBackStackEntry!!
        navStack.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            if (navStack.savedStateHandle.contains(DIALOG_CLICK_EVENT)) {
                val click = navStack.savedStateHandle.get<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                handleDialogClick(click)
            }
        })
    }

    override fun onBackPressed() {
        if (activityViewModel.onBackPressed()) {
            return
        }

        super.onBackPressed()
    }

    private fun handleDialogClick(click: DialogClickEvent) {
        if (click.dialogBtn == DialogButton.POSITIVE) {
            when (click.dialogId) {
                DISCARD_DIALOG_ID -> {
                    val args =
                        bundleOf(KEY_CREATE_REPORT_RESULT to getString(R.string.boarding_canceled))
                    navigation.navigate(R.id.action_tabsFragment_to_home_navigation, args)
                    this.finish()
                }
            }
        }
    }

    private fun showDiscardReportDialog() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            DISCARD_DIALOG_ID,
            getString(R.string.cancel_boarding),
            getString(R.string.board_not_saved),
            getString(R.string.menu_cancel_report),
            getString(R.string.keep_editing)
        ).bundle()

        navigation.navigate(R.id.confirmation_dialog, dialogBundle)
    }
}