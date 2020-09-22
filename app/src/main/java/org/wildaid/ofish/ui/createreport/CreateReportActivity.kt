package org.wildaid.ofish.ui.createreport

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.findNavController
import kotlinx.coroutines.handleCoroutineException
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.OnSaveListener
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
        activityViewModel.createReportUserEvent.observe(this, EventObserver(::handleUserEvent))

        subscribeToDialogEvents()
    }

    override fun onBackPressed() {
        if (activityViewModel.onBackPressed()) {
            return
        }

        super.onBackPressed()
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

    private fun handleUserEvent(event:  CreateReportViewModel.CreateReportUserEvent) {
        when(event) {
            CreateReportViewModel.CreateReportUserEvent.AskDiscardBoarding -> askDiscardReport()
        }
    }

    private fun askDiscardReport() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            DISCARD_DIALOG_ID,
            getString(R.string.cancel_boarding),
            getString(R.string.board_not_saved),
            getString(R.string.keep_editing),
            getString(R.string.save_and_finish_later)
        ).apply {
            neutral = getString(R.string.menu_cancel_report)
            highLightNegative = true
        }.bundle()

        navigation.navigate(R.id.confirmation_dialog, dialogBundle)
    }

    private fun handleDialogClick(click: DialogClickEvent) {
        when (click.dialogId) {
            DISCARD_DIALOG_ID -> {
                if (click.dialogBtn == DialogButton.NEUTRAL) {
                    val args = bundleOf(KEY_CREATE_REPORT_RESULT to getString(R.string.boarding_canceled))
                    navigation.navigate(R.id.action_tabsFragment_to_home_navigation, args)
                    this.finish()
                } else if (click.dialogBtn == DialogButton.NEGATIVE) {
                    activityViewModel.saveReport(isDraft = true, listener = object :
                        OnSaveListener {
                        override fun onSuccess() {
                            val args = bundleOf(KEY_CREATE_REPORT_RESULT to getString(R.string.draft_saved))
                            navigation.navigate(R.id.action_tabsFragment_to_home_navigation, args)
                            finish()
                        }

                        override fun onError(it: Throwable) {
                             Log.e("Save error", it.message ?: "")
                        }
                    })
                }
            }
        }
    }
}