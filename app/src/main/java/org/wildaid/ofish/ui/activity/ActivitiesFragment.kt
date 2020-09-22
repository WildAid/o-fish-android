package org.wildaid.ofish.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_activities.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentActivitiesBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.simple.SimpleSearchFragment
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.setVisible

class ActivitiesFragment : BaseReportFragment(R.layout.fragment_activities) {
    private val fragmentViewModel: ActivitiesViewModel by viewModels { getViewModelFactory() }
    private lateinit var fragmentDataBinding: FragmentActivitiesBinding
    private lateinit var requiredFields: Array<TextInputLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToSearchResult()
        fragmentViewModel.initActivities(currentReport, currentReportPhotos)
    }

    override fun isAllRequiredFieldsNotEmpty(): Boolean {
        requiredFields.forEach {
            val text = it.editText?.text
            if (it.visibility == View.VISIBLE && text.isNullOrBlank()) {
                return false
            }
        }
        return true
    }

    override fun validateForms(): Boolean {
        var result = true
        requiredFields.forEach {
            val text = it.editText?.text
            if (it.visibility == View.VISIBLE && text.isNullOrBlank()) {
                result = false
                it.errorIconDrawable = resources.getDrawable(R.drawable.ic_error_outline, null)
            }
        }
        isFieldCheckPassed = true
        return result
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentDataBinding = FragmentActivitiesBinding.bind(view)
            .apply {
                this.viewModel = fragmentViewModel
                this.lifecycleOwner = this@ActivitiesFragment.viewLifecycleOwner
            }

        requiredFields = arrayOf(
            activities_edit_text_layout,
            activity_fishery_edit_text_layout,
            activity_gear_edit_layout
        )

        fragmentViewModel.activitiesUserEvents.observe(
            viewLifecycleOwner,
            EventObserver(::handleUserEvent)
        )

        fragmentViewModel.activityItemLiveData.observe(viewLifecycleOwner, Observer {
            fragmentDataBinding.activitiesNoteLayout.setVisible(it.attachments.hasNotes())
        })

        fragmentViewModel.fisheryItemLiveData.observe(viewLifecycleOwner, Observer {
            fragmentDataBinding.activitiesFisheryNoteLayout.setVisible(it.attachments.hasNotes())
        })

        fragmentViewModel.gearItemLiveData.observe(viewLifecycleOwner, Observer {
            fragmentDataBinding.activityGearNoteLayout.setVisible(it.attachments.hasNotes())
        })

        fragmentDataBinding.activitiesPhotosLayout.onPhotoClickListener = ::showFullImage
        fragmentDataBinding.fisheryPhotosLayout.onPhotoClickListener = ::showFullImage
        fragmentDataBinding.gearPhotosLayout.onPhotoClickListener = ::showFullImage

        fragmentDataBinding.activitiesPhotosLayout.onPhotoRemoveListener =
            fragmentViewModel::removePhotoFromActivity
        fragmentDataBinding.fisheryPhotosLayout.onPhotoRemoveListener =
            fragmentViewModel::removePhotoFromFishery
        fragmentDataBinding.gearPhotosLayout.onPhotoRemoveListener =
            fragmentViewModel::removePhotoFromGear
    }

    private fun handleUserEvent(event: ActivitiesViewModel.ActivitiesUserEvent) {
        when (event) {
            ActivitiesViewModel.ActivitiesUserEvent.ChooseActivityEvent -> {
                val bundle =
                    bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to SimpleSearchFragment.SearchActivity)
                navigation.navigate(R.id.action_tabsFragment_to_simple_search, bundle)
            }
            ActivitiesViewModel.ActivitiesUserEvent.ChooseFisheryEvent -> {
                val bundle =
                    bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to SimpleSearchFragment.SearchFishery)
                navigation.navigate(R.id.action_tabsFragment_to_simple_search, bundle)
            }
            ActivitiesViewModel.ActivitiesUserEvent.ChooseGearEvent -> {
                val bundle =
                    bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to SimpleSearchFragment.SearchGear)
                navigation.navigate(R.id.action_tabsFragment_to_simple_search, bundle)
            }

            ActivitiesViewModel.ActivitiesUserEvent.NextEvent -> {
                if (isFieldCheckPassed || validateForms()) {
                    onNextListener.onNextClicked()
                } else {
                    showSnackbarWarning()
                }
            }

            ActivitiesViewModel.ActivitiesUserEvent.AddActivityAttachmentEvent -> {
                askForAttachmentType(
                    onNoteSelected = fragmentViewModel::addNoteForActivity,
                    onPhotoSelected = fragmentViewModel::addPhotoForActivity
                )
            }
            ActivitiesViewModel.ActivitiesUserEvent.AddFisheryAttachmentEvent -> {
                askForAttachmentType(
                    onNoteSelected = fragmentViewModel::addNoteForFishery,
                    onPhotoSelected = fragmentViewModel::addPhotoForFishery
                )
            }
            ActivitiesViewModel.ActivitiesUserEvent.AddGearAttachmentEvent -> {
                askForAttachmentType(
                    onNoteSelected = fragmentViewModel::addNoteForGear,
                    onPhotoSelected = fragmentViewModel::addPhotoForGear
                )
            }
        }
    }

    private fun subscribeToSearchResult() {
        val navBackStackEntry = navigation.currentBackStackEntry!!
        navBackStackEntry.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) {
                return@LifecycleEventObserver
            }

            val savedState = navBackStackEntry.savedStateHandle
            val resultEntityType = savedState.get<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)
                ?: return@LifecycleEventObserver

            when (resultEntityType) {
                is SimpleSearchFragment.SearchFishery -> {
                    val fishery =
                        navBackStackEntry.savedStateHandle.remove<String>(BaseSearchFragment.SEARCH_RESULT)
                    fragmentViewModel.updateFishery(fishery.orEmpty())
                }

                is SimpleSearchFragment.SearchActivity -> {
                    val activity =
                        navBackStackEntry.savedStateHandle.remove<String>(BaseSearchFragment.SEARCH_RESULT)
                    fragmentViewModel.updateActivity(activity.orEmpty())
                }

                is SimpleSearchFragment.SearchGear -> {
                    val gear =
                        navBackStackEntry.savedStateHandle.remove<String>(BaseSearchFragment.SEARCH_RESULT)
                    fragmentViewModel.updateGear(gear.orEmpty())
                }
                else -> return@LifecycleEventObserver
            }
            savedState.remove<Any>(BaseSearchFragment.SEARCH_ENTITY_KEY)
        })
    }
}