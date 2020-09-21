package org.wildaid.ofish.ui.tabs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.CheckedTextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tabs.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.OnSaveListener
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.*
import org.wildaid.ofish.ui.createreport.CreateReportBundle
import org.wildaid.ofish.ui.createreport.CreateReportViewModel
import org.wildaid.ofish.ui.createreport.KEY_CREATE_REPORT_ARGS
import org.wildaid.ofish.ui.crew.CrewFragment
import org.wildaid.ofish.ui.home.KEY_CREATE_REPORT_RESULT
import org.wildaid.ofish.ui.vessel.VesselFragment
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.setVisible
import org.wildaid.ofish.util.showSnackMessage

const val BASIC_INFO_FRAGMENT_POSITION = 0
const val VESSEL_FRAGMENT_POSITION = 1
const val CREW_FRAGMENT_POSITION = 2
const val ACTIVITIES_FRAGMENT_POSITION = 3
const val CATCH_FRAGMENT_POSITION = 4
const val VIOLATION_FRAGMENT_POSITION = 5
const val RISK_FRAGMENT_POSITION = 6
const val NOTES_FRAGMENT_POSITION = 7

const val FRAGMENT_TAG_PREFIX = "f"
private const val ASK_PREFILL_VESSEL_DIALOG_ID = 13
private const val ASK_SKIP_TABS_DIALOG_ID = 14
private const val SUBMIT_DIALOG_ID = 15
private const val ASK_PREFILL_CREW_DIALOG_ID = 191232

class TabsFragmentHost : Fragment(R.layout.fragment_tabs), OnNextClickedListener {
    private val fragmentViewModel by viewModels<TabsViewModel> { getViewModelFactory() }
    private val activityViewModel by activityViewModels<CreateReportViewModel> { getViewModelFactory() }
    private val navigation by lazy { findNavController() }

    private lateinit var fragmentFactory: TabsFragmentFactory
    private lateinit var tabsFragmentAdapter: TabsFragmentAdapter
    private lateinit var currentReportFragment: BaseReportFragment
    private var pendingSkippingTabs: List<TabItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments: CreateReportBundle? =
            requireArguments().getParcelable(KEY_CREATE_REPORT_ARGS)

        fragmentViewModel.initReport(
            activityViewModel.report,
            activityViewModel.reportPhotos,
            arguments?.prefillVessel,
            arguments?.prefillCrew
        )
        subscribeToDialogEvents()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_create_report, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activityViewModel.onBackPressed()
            R.id.menu_report_submit -> showSubmitReportDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentViewModel.reportLiveData.observe(viewLifecycleOwner, Observer {
            initUI(it.first, it.second)
        })

        fragmentViewModel.userEventLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is TabsViewModel.TabsUserEvent.AskSkipSectionsEvent -> showSkipSectionsDialog(it.skippedTabs)
                is TabsViewModel.TabsUserEvent.AskLeftEmptyFields -> showSkipSectionsDialog(it.skippedTabs)
                is TabsViewModel.TabsUserEvent.ChangeTabEvent -> selectTab(it.tabItem)
                TabsViewModel.TabsUserEvent.AskPrefillVesselEvent -> showAskPrefillBoatDialog()
                TabsViewModel.TabsUserEvent.AskPrefillCrewEvent -> showAskPrefillCrewDialog()
            }
        })

        fragmentViewModel.tabsStateLiveData.observe(viewLifecycleOwner, Observer {
            updateTabsDrawable(it)
        })
    }

    override fun onNextClicked(isSubmitted: Boolean) {
        if (isSubmitted) {
            showSubmitReportDialog()
        } else {
            tabs_pager.currentItem += 1
        }
    }

    private fun selectTab(tabItem: TabItem) {
        tabs_layout.getTabAt(tabItem.position)?.select()
    }

    private fun onTabChanged(previousTabIndex: Int, currentTabIndex: Int) {
        currentReportFragment =
            childFragmentManager.findFragmentByTag("$FRAGMENT_TAG_PREFIX${currentTabIndex}") as BaseReportFragment

        val previousReportFragment =
            childFragmentManager.findFragmentByTag("f$previousTabIndex") as BaseReportFragment?

        previousReportFragment?.let {
            fragmentViewModel.onTabChanged(
                previousTabIndex,
                currentTabIndex
            )
        }
    }

    private fun updateTabsDrawable(tabs: List<TabItem>) {
        tabs.forEach { tabState ->
            if (tabState.status == TabStatus.NOT_VISITED) {
                tabs_layout.getTabAt(tabState.position)?.customView?.also {
                    val textSelector = resources.getColorStateList(R.color.selector_tabs_text, null)
                    it.findViewById<CheckedTextView>(android.R.id.text1)?.setTextColor(textSelector)
                    it.findViewById<View>(R.id.tab_bottom_line)?.setVisible(false)
                }
            } else {
                val visitedStatusColorRes =
                    if (tabState.status == TabStatus.SKIPPED) R.color.tabs_amber else R.color.main_blue
                val visitedStatusColor = resources.getColor(visitedStatusColorRes, null)

                tabs_layout.getTabAt(tabState.position)?.customView?.also { view ->
                    view.findViewById<View>(R.id.tab_bottom_line)?.setVisible(true)
                    view.findViewById<View>(R.id.tab_bottom_line)
                        ?.setBackgroundColor(visitedStatusColor)
                    view.findViewById<CheckedTextView>(android.R.id.text1)
                        ?.setTextColor(visitedStatusColor)
                }
            }
        }
    }

    private fun subscribeToDialogEvents() {
        val navStack = navigation.currentBackStackEntry!!
        navStack.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            if (navStack.savedStateHandle.contains(DIALOG_CLICK_EVENT)) {
                val click = navStack.savedStateHandle.get<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                val handled = handleDialogClick(click)
                if (handled) {
                    navStack.savedStateHandle.remove<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                }
            }
        })
    }

    private fun showSkipSectionsDialog(skippedTabs: List<TabItem>) {
        pendingSkippingTabs = skippedTabs

        val skipMessage = skippedTabs.joinToString("\n") { "- ${it.title}" }

        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ASK_SKIP_TABS_DIALOG_ID,
            getString(R.string.skip_sections_dialog_title),
            skipMessage,
            getString(R.string.skip_sections_dialog_yes),
            getString(R.string.keep_editing)
        ).bundle()

        navigation.navigate(R.id.confirmation_dialog, dialogBundle)
    }

    private fun showAskPrefillBoatDialog() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ASK_PREFILL_VESSEL_DIALOG_ID,
            getString(R.string.prefill_vessel_dialog_title),
            getString(R.string.prefill_vessel_dialog_description),
            getString(R.string.prefill_vessel_dialog_yes),
            getString(R.string.no)
        ).bundle()

        navigation.navigate(R.id.confirmation_dialog, dialogBundle)
    }

    private fun showAskPrefillCrewDialog() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ASK_PREFILL_CREW_DIALOG_ID,
            getString(R.string.prefill_crew_dialog_title),
            getString(R.string.prefill_crew_dialog_description),
            getString(R.string.prefill_crew_dialog_yes),
            getString(R.string.no)
        ).bundle()

        navigation.navigate(R.id.confirmation_dialog, dialogBundle)
    }

    private fun showSubmitReportDialog() {
        val tabs = fragmentViewModel.getSkippedAndNotVisitedTabs()
        val title: String
        val message: String
        if (tabs.isEmpty()) {
            title = getString(R.string.submit_boarding)
            message = getString(R.string.sure_to_submit)
        } else {
            title = getString(R.string.you_left_blank)
            message = getString(
                R.string.sure_to_submit_with_skipped,
                tabs.joinToString("\n") { "- ${it.title}" })
        }
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            SUBMIT_DIALOG_ID,
            title,
            message,
            getString(R.string.menu_submit_report),
            getString(R.string.keep_editing)
        ).bundle()

        navigation.navigate(R.id.confirmation_dialog, dialogBundle)
    }

    private fun handleDialogClick(click: DialogClickEvent): Boolean {
        var clickHandled = false
        if (click.dialogBtn == DialogButton.POSITIVE) {
            when (click.dialogId) {
                ASK_PREFILL_VESSEL_DIALOG_ID -> {
                    val vesselFragment =
                        childFragmentManager.findFragmentByTag("$FRAGMENT_TAG_PREFIX$VESSEL_FRAGMENT_POSITION") as VesselFragment
                    fragmentViewModel.vesselToPrefill?.let {
                        vesselFragment.fillVesselInfo(it)
                    }
                    clickHandled = true
                }
                ASK_PREFILL_CREW_DIALOG_ID -> {
                    val crewFragment =
                        childFragmentManager.findFragmentByTag("$FRAGMENT_TAG_PREFIX$CREW_FRAGMENT_POSITION") as CrewFragment
                    crewFragment.fillCrewInfo(
                        fragmentViewModel.prefillCaptain!!,
                        fragmentViewModel.crewToPrefill!!
                    )

                    clickHandled = true
                }
                SUBMIT_DIALOG_ID -> {
                    activityViewModel.saveReport(isDraft = false, listener = object : OnSaveListener {
                        override fun onSuccess() {
                            val args = bundleOf(KEY_CREATE_REPORT_RESULT to getString(R.string.boarding_submitted))
                            navigation.navigate(R.id.action_tabsFragment_to_home_navigation, args)
                            requireActivity().finish()
                        }

                        override fun onError(it: Throwable) {
                            Log.e("Save error", it.message ?: "")
                            showSnackMessage(requireView(), getString(R.string.saving_error))
                        }
                    })

                    clickHandled = true
                }

                ASK_SKIP_TABS_DIALOG_ID -> {
                    if (pendingSkippingTabs != null) {
                        fragmentViewModel.onTabsSkipped(pendingSkippingTabs.orEmpty())
                        pendingSkippingTabs = null
                    }

                    clickHandled = true
                }
            }
        }

        return clickHandled
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI(report: Report, reportPhotos: MutableList<PhotoItem>) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(tabs_toolbar)
        tabs_toolbar.setNavigationIcon(R.drawable.ic_close_white)

        fragmentFactory = TabsFragmentFactory(requireContext(), this, report, reportPhotos)
        tabsFragmentAdapter = TabsFragmentAdapter(fragmentFactory, childFragmentManager, lifecycle)
        tabs_pager.apply {
            adapter = tabsFragmentAdapter
            offscreenPageLimit = 8
            isUserInputEnabled = false
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                private var lastSelectedTabPosition = TabLayout.Tab.INVALID_POSITION

                override fun onPageSelected(position: Int) {
                    onTabChanged(lastSelectedTabPosition, position)
                    lastSelectedTabPosition = position
                }
            })
        }

        TabLayoutMediator(tabs_layout, tabs_pager, fragmentFactory.fragmentTitleConfigurator)
            .attach()

        // Unable to use tabs_layout.addOnTabSelectedListener() since we need to intercept click
        val tabTouchClickDetector = GestureTouchClickDetector(requireContext())
        val tabStrip = tabs_layout.getChildAt(0) as LinearLayout
        for (newPosition in 0 until tabStrip.childCount) {
            val currentTabPosition = tabs_layout.selectedTabPosition
            tabStrip.getChildAt(newPosition).setOnLongClickListener {
                return@setOnLongClickListener currentTabPosition != newPosition &&
                        fragmentViewModel.onTabClicked(
                            currentTabPosition,
                            newPosition,
                            currentReportFragment.isAllRequiredFieldsNotEmpty()
                        )
            }

            tabStrip.getChildAt(newPosition).setOnTouchListener { _, event ->
                val isClick = tabTouchClickDetector.onTouchEvent(event)
                val currentTabPosition = tabs_layout.selectedTabPosition
                return@setOnTouchListener currentTabPosition != newPosition &&
                        isClick &&
                        fragmentViewModel.onTabClicked(
                            currentTabPosition,
                            newPosition,
                            currentReportFragment.isAllRequiredFieldsNotEmpty()
                        )
            }
        }
    }
}