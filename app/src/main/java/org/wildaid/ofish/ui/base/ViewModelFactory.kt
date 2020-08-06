package org.wildaid.ofish.ui.base

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.ui.activity.ActivitiesViewModel
import org.wildaid.ofish.ui.basicinformation.BasicInformationViewModel
import org.wildaid.ofish.ui.catches.CatchViewModel
import org.wildaid.ofish.ui.createreport.CreateReportViewModel
import org.wildaid.ofish.ui.crew.CrewViewModel
import org.wildaid.ofish.ui.fullimage.FullImageViewModel
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.ui.home.HomeFragmentViewModel
import org.wildaid.ofish.ui.login.LoginViewModel
import org.wildaid.ofish.ui.notes.NotesViewModel
import org.wildaid.ofish.ui.patrolsummary.PatrolSummaryViewModel
import org.wildaid.ofish.ui.reportdetail.ReportDetailViewModel
import org.wildaid.ofish.ui.risk.RiskViewModel
import org.wildaid.ofish.ui.search.complex.ComplexSearchViewModel
import org.wildaid.ofish.ui.search.simple.SimpleSearchViewModel
import org.wildaid.ofish.ui.splash.SplashViewModel
import org.wildaid.ofish.ui.tabs.TabsViewModel
import org.wildaid.ofish.ui.vessel.VesselViewModel
import org.wildaid.ofish.ui.vesseldetails.VesselDetailsViewModel
import org.wildaid.ofish.ui.violation.AddCrewViewModel
import org.wildaid.ofish.ui.violation.ViolationViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val repository: Repository,
    private val application: Application,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            // Splash
            isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel(repository)
            // Login
            isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(repository)
            // Home
            isAssignableFrom(HomeActivityViewModel::class.java) ->
                HomeActivityViewModel(repository, application)
            isAssignableFrom(HomeFragmentViewModel::class.java) ->
                HomeFragmentViewModel(repository)
            isAssignableFrom(PatrolSummaryViewModel::class.java) ->
                PatrolSummaryViewModel(repository)

            // Create report
            isAssignableFrom(CreateReportViewModel::class.java) ->
                CreateReportViewModel(repository)
            isAssignableFrom((TabsViewModel::class.java)) ->
                TabsViewModel(repository, application)
            isAssignableFrom((BasicInformationViewModel::class.java)) ->
                BasicInformationViewModel()
            isAssignableFrom(VesselViewModel::class.java) ->
                VesselViewModel(repository)
            isAssignableFrom(CrewViewModel::class.java) ->
                CrewViewModel(repository, application)
            isAssignableFrom(ActivitiesViewModel::class.java) ->
                ActivitiesViewModel(repository)
            isAssignableFrom(CatchViewModel::class.java) ->
                CatchViewModel(repository, application)
            isAssignableFrom(ViolationViewModel::class.java) ->
                ViolationViewModel(repository, application)
            isAssignableFrom(AddCrewViewModel::class.java) ->
                AddCrewViewModel()
            isAssignableFrom(RiskViewModel::class.java) ->
                RiskViewModel(application)
            isAssignableFrom(NotesViewModel::class.java) ->
                NotesViewModel(repository, application)

            // Report Details
            isAssignableFrom(ReportDetailViewModel::class.java) ->
                ReportDetailViewModel(repository)

            // Vessel Details
            isAssignableFrom(VesselDetailsViewModel::class.java) ->
                VesselDetailsViewModel(repository, application)

            // Search
            isAssignableFrom(SimpleSearchViewModel::class.java) ->
                SimpleSearchViewModel(repository, application)
            isAssignableFrom(ComplexSearchViewModel::class.java) ->
                ComplexSearchViewModel(repository, application)

            isAssignableFrom(FullImageViewModel::class.java) ->
                FullImageViewModel(repository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}