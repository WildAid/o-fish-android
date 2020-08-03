package org.wildaid.ofish.ui.fullimage

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentFullImageBinding
import org.wildaid.ofish.ui.base.PHOTO_ID
import org.wildaid.ofish.ui.createreport.CreateReportViewModel
import org.wildaid.ofish.util.getViewModelFactory

class FullImageFragment : Fragment(R.layout.fragment_full_image) {

    private val fragmentViewModel: FullImageViewModel by viewModels { getViewModelFactory() }
    private val activityViewModel: CreateReportViewModel by activityViewModels { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentFullImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentFullImageBinding.bind(view).apply {
            this.lifecycleOwner = this@FullImageFragment.viewLifecycleOwner
        }

        val photoId = arguments?.get(PHOTO_ID) as String
        ViewCompat.setTransitionName(viewDataBinding.fullImage, photoId)

        fragmentViewModel.photoLiveData.observe(viewLifecycleOwner, Observer {
            Glide.with(this)
                .load(it.localUri ?: it.photo.getResourceForLoading())
                .into(viewDataBinding.fullImage)
                .apply {
                    RequestOptions().dontTransform()
                }
        })

        fragmentViewModel.findPhoto(activityViewModel.reportPhotos, photoId)
        viewDataBinding.executePendingBindings()
    }

    override fun onStart() {
        super.onStart()
        activityViewModel.isOnSearch = true
    }

    override fun onStop() {
        super.onStop()
        activityViewModel.isOnSearch = false
    }
}