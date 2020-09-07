package org.wildaid.ofish.ui.tabs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.tabs.TabLayout
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Boat
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.ui.createreport.PrefillCrew
import org.wildaid.ofish.ui.createreport.PrefillVessel
import org.wildaid.ofish.util.getString

class TabsViewModel(val repository: Repository, application: Application) :
    AndroidViewModel(application) {

    private var _reportLiveData = MutableLiveData<Pair<Report, MutableList<PhotoItem>>>()
    val reportLiveData: LiveData<Pair<Report, MutableList<PhotoItem>>>
        get() = _reportLiveData

    private var _userEventLiveData = MutableLiveData<Event<UserEvent>>()
    val userEventLiveData: LiveData<Event<UserEvent>>
        get() = _userEventLiveData

    private var _tabsStateLiveData = MutableLiveData<List<TabItem>>()
    val tabsStateLiveData: LiveData<List<TabItem>>
        get() = _tabsStateLiveData

    var vesselToPrefill: Boat? = null
    var crewToPrefill: List<CrewMember>? = null
    var prefillCaptain: CrewMember? = null
    private var vesselFragmentWasVisited: Boolean = false
    private var crewFragmentWasVisited: Boolean = false
    private lateinit var tabs: List<TabItem>
    private lateinit var report: Report

    fun initReport(
        creationReport: Report,
        reportPhotos: MutableList<PhotoItem>,
        vesselToPrefill: PrefillVessel?,
        crewToPrefill: PrefillCrew?
    ) {
        initTabStates()

        vesselToPrefill?.let {
            this.vesselToPrefill = Boat().apply {
                this.name = vesselToPrefill.vesselName
                this.homePort = vesselToPrefill.homePort
                this.nationality = vesselToPrefill.flagState
                this.permitNumber = vesselToPrefill.vesselNumber
            }
        }

        crewToPrefill?.let {
            prefillCaptain = CrewMember().apply {
                name = it.captain.first
                license = it.captain.second
            }

            this.crewToPrefill = mutableListOf(
                *it.crew.map { pair ->
                    CrewMember().apply {
                        name = pair.first
                        license = pair.second
                    }
                }.toTypedArray()
            )
        }

        this.report = creationReport
        this._reportLiveData.value = creationReport to reportPhotos
    }

    fun onTabsSkipped(skippedTabs: List<TabItem>) {
        skippedTabs.forEach {
            it.status = TabStatus.SKIPPED
        }

        _tabsStateLiveData.value = tabs
        val nextTab = tabs[tabs.indexOf(skippedTabs.last()) + 1]
        _userEventLiveData.value = Event(UserEvent.ChangeTabEvent(nextTab))
    }

    fun onTabClicked(
        currentTabPosition: Int,
        newPosition: Int,
        currentFormValid: Boolean
    ): Boolean {
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
            _userEventLiveData.value = Event(UserEvent.AskSkipSectionsEvent(notVisitedTabs))
            return true
        }

        if (!currentFormValid && newPosition > currentTabPosition) {
            _userEventLiveData.value = Event(UserEvent.AskLeftEmptyFields(listOf(currentTab)))
            return true
        }

        return false
    }

    fun onTabChanged(previousTabIndex: Int, currentTabIndex: Int) {
        if (!vesselFragmentWasVisited && currentTabIndex == VESSEL_FRAGMENT_POSITION && vesselToPrefill != null) {
            vesselFragmentWasVisited = true
            _userEventLiveData.value = Event(UserEvent.AskPrefillVesselEvent)
        }

        if (!crewFragmentWasVisited && currentTabIndex == CREW_FRAGMENT_POSITION && crewToPrefill != null) {
            crewFragmentWasVisited = true
            _userEventLiveData.value = Event(UserEvent.AskPrefillCrewEvent)
        }

        val previousTab = tabs[previousTabIndex]
        if (previousTabIndex != TabLayout.Tab.INVALID_POSITION && previousTab.status != TabStatus.SKIPPED) {
            previousTab.apply {
                status = TabStatus.VISITED
            }
            _tabsStateLiveData.value = tabs
        }
        tabs[currentTabIndex].apply {
            status = TabStatus.VISITED
        }

        _tabsStateLiveData.value = tabs
    }

    private fun initTabStates() {
        tabs = listOf(
            TabItem(
                BASIC_INFO_FRAGMENT_POSITION,
                getString(R.string.basic_information),
                TabStatus.VISITED
            ),
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
        object AskPrefillCrewEvent : UserEvent()
    }
}