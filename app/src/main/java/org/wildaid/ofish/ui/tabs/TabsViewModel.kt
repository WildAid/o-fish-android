package org.wildaid.ofish.ui.tabs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.material.tabs.TabLayout
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.*
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

class TabsViewModel(val repository: Repository, application: Application) :
    AndroidViewModel(application) {
    val reportLiveData = MutableLiveData<Pair<Report, MutableList<PhotoItem>>>()
    val userEventLiveData = MutableLiveData<Event<UserEvent>>()
    val tabsStateLiveData = MutableLiveData<List<TabItem>>()

    var vesselToPrefill: Boat? = null
    private var vesselFragmentWasVisited: Boolean = false
    private lateinit var tabs: List<TabItem>
    private lateinit var report: Report

    fun initReport(report: Report, reportPhotos: MutableList<PhotoItem>, vesselId: String?) {
        if (vesselId != null) {
            this.vesselToPrefill = repository.findBoat(vesselId)
        } else {
            this.vesselToPrefill = null
        }

        initTabStates()

        this.report = report
        this.reportLiveData.value = report to reportPhotos
    }

    fun onTabsSkipped(skippedTabs: List<TabItem>) {
        skippedTabs.forEach {
            it.status = TabStatus.SKIPPED
        }

        tabsStateLiveData.value = tabs
        val nextTab = tabs[tabs.indexOf(skippedTabs.last()) + 1]
        userEventLiveData.value = Event(UserEvent.ChangeTabEvent(nextTab))
    }

    fun onTabClicked(currentTabPosition: Int, newPosition: Int, currentFormValid: Boolean): Boolean {
        val notVisitedTabs = mutableListOf<TabItem>()
        tabs.forEachIndexed { index, tab ->
            if (tab.status == TabStatus.NOT_VISITED && newPosition > index) {
                notVisitedTabs.add(tab)
            }
        }

        val currentTab = tabs[currentTabPosition]
        if (notVisitedTabs.isNotEmpty()) {
            if (!currentFormValid) {
                notVisitedTabs.add(0, currentTab)
            }
            userEventLiveData.value = Event(UserEvent.AskSkipSectionsEvent(notVisitedTabs))
            return true
        }

        if (!currentFormValid && newPosition > currentTabPosition) {
            userEventLiveData.value = Event(UserEvent.AskLeftEmptyFields(listOf(currentTab)))
            return true
        }

        return false
    }

    fun onTabChanged(previousTabIndex: Int, currentTabIndex: Int) {
        if (!vesselFragmentWasVisited && currentTabIndex == VESSEL_FRAGMENT_POSITION && vesselToPrefill != null) {
            vesselFragmentWasVisited = true
            userEventLiveData.value = Event(UserEvent.AskPrefillVesselEvent)
        }

        val previousTab = tabs[previousTabIndex]
        if (previousTabIndex != TabLayout.Tab.INVALID_POSITION && previousTab.status != TabStatus.SKIPPED) {
            previousTab.apply {
                status = TabStatus.VISITED
            }
            tabsStateLiveData.value = tabs
        }
        tabs[currentTabIndex].apply {
            status = TabStatus.VISITED
        }

        tabsStateLiveData.value = tabs
    }

    private fun initTabStates() {
        tabs = listOf(
            TabItem(BASIC_INFO_FRAGMENT_POSITION, getString(R.string.basic_information), TabStatus.VISITED),
            TabItem(VESSEL_FRAGMENT_POSITION, getString(R.string.vessel)),
            TabItem(CREW_FRAGMENT_POSITION, getString(R.string.crew)),
            TabItem(ACTIVITIES_FRAGMENT_POSITION, getString(R.string.activity)),
            TabItem(CATCH_FRAGMENT_POSITION, getString(R.string.catch_title)),
            TabItem(VIOLATION_FRAGMENT_POSITION, getString(R.string.violation)),
            TabItem(RISK_FRAGMENT_POSITION, getString(R.string.risk)),
            TabItem(NOTES_FRAGMENT_POSITION, getString(R.string.notes))
        )
    }

    fun getSkippedAndNotVisitedTabs(): List<TabItem> {
        return tabs.filterNot { it.status == TabStatus.VISITED }
    }

    sealed class UserEvent {
        class AskSkipSectionsEvent(var skippedTabs: List<TabItem>) : UserEvent()
        class AskLeftEmptyFields(var skippedTabs: List<TabItem>) : UserEvent()
        class ChangeTabEvent(var tabItem: TabItem) : UserEvent()
        object AskPrefillVesselEvent : UserEvent()
    }
}