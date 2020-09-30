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
private const val DELETE_DRAFT_DIALOG_ID = 26
const val SHOULD_NAVIGATE_TO_DRAFT_LIST = "should_navigate_to_draft_list"

class CreateReportActivity : AppCompatActivity() {
    private val activityViewModel: CreateReportViewModel by viewModels { getViewModelFactory() }
    private lateinit var navigation: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_report)
        navigation = findNavController(R.id.create_report_host_fragment)

        val createReportBundle =
            intent.extras?.getParcelable<CreateReportBundle?>(KEY_CREATE_REPORT_ARGS)
        activityViewModel.initReport(createReportBundle?.reportDraftId)
        activityViewModel.createReportUserEvent.observe(this, EventObserver(::handleUserEvent))
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
                if (handleDialogClick(click)) {
                    navStack.savedStateHandle.remove<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                }
            }
        })
    }

    private fun handleUserEvent(event: CreateReportViewModel.CreateReportUserEvent) {
        when (event) {
            CreateReportViewModel.CreateReportUserEvent.AskDeleteDraft -> askDiscardDraft()
            CreateReportViewModel.CreateReportUserEvent.AskDiscardBoarding -> askDiscardReport()
            CreateReportViewModel.CreateReportUserEvent.StartReportCreation -> displayCreationTabs()
            CreateReportViewModel.CreateReportUserEvent.NavigateToDraftList -> navigateToDraftList()
        }
    }

    private fun displayCreationTabs() {
        navigation.setGraph(R.navigation.create_report_navigation, intent.extras)
        subscribeToDialogEvents()
    }

    private fun askDiscardDraft() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            DELETE_DRAFT_DIALOG_ID,
            getString(R.string.cancel_boarding),
            getString(R.string.board_not_saved),
            getString(R.string.keep_editing),
            getString(R.string.save_and_finish_later)
        ).apply {
            neutral = getString(R.string.delete_draft)
            highLightNegative = true
        }.bundle()

        navigation.navigate(R.id.confirmation_dialog, dialogBundle)
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

    private fun handleDialogClick(click: DialogClickEvent) :  Boolean{
        return when (click.dialogId) {
            DISCARD_DIALOG_ID -> {
                if (click.dialogBtn == DialogButton.NEUTRAL) {
                    navigateHome(getString(R.string.boarding_canceled))
                } else if (click.dialogBtn == DialogButton.NEGATIVE) {
                    activityViewModel.saveReport(isDraft = true, listener = object :
                        OnSaveListener {
                        override fun onSuccess() {
                            navigateHome(getString(R.string.draft_saved))
                        }

                        override fun onError(it: Throwable) {
                            Log.e("Save error", it.message ?: "")
                        }
                    })
                }
                true
            }
            DELETE_DRAFT_DIALOG_ID -> {
                if (click.dialogBtn == DialogButton.NEUTRAL) {
                    activityViewModel.deleteReport()
                } else if (click.dialogBtn == DialogButton.NEGATIVE) {
                    activityViewModel.saveReport(isDraft = true, listener = object :
                        OnSaveListener {
                        override fun onSuccess() {
                            navigateHome(getString(R.string.draft_saved))
                        }

                        override fun onError(it: Throwable) {
                            Log.e("Save error", it.message ?: "")
                        }
                    })
                }
                true
            }
            else -> false
        }
    }

    private fun navigateHome(resultMessage: String) {
        val args =
            bundleOf(KEY_CREATE_REPORT_RESULT to resultMessage)
        navigation.navigate(R.id.action_tabsFragment_to_home_navigation, args)
        this.finish()
    }

    private fun navigateToDraftList() {
        val args = bundleOf(
            KEY_CREATE_REPORT_RESULT to getString(R.string.draft_deleted),
            SHOULD_NAVIGATE_TO_DRAFT_LIST to true
        )
        navigation.navigate(R.id.action_tabsFragment_to_home_navigation, args)
        this.finish()
    }
}