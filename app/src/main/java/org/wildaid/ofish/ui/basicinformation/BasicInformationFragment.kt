package org.wildaid.ofish.ui.basicinformation

import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.databinding.FragmentBasicInformationBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard


class BasicInformationFragment : BaseReportFragment(R.layout.fragment_basic_information),
    OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewDataBinding: FragmentBasicInformationBinding

    private lateinit var map: GoogleMap
    private lateinit var marker: Marker

    private val fragmentViewModel: BasicInformationViewModel by viewModels { getViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentViewModel.initReport(currentReport)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        subscribeToNavigationResult()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentBasicInformationBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = this@BasicInformationFragment.viewLifecycleOwner
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.basic_info_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fragmentViewModel.buttonId.observe(
            viewLifecycleOwner, EventObserver(::onButtonClicked)
        )
    }

    private fun initMap(location: Location) {
        val coord = LatLng(location.latitude, location.longitude)
        if (!::marker.isInitialized) {
            marker = map.addMarker(
                MarkerOptions()
                    .position(coord)
            )
        }
        map.isMyLocationEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 10f))
    }

    private fun subscribeToNavigationResult() {
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map

        this.map.setOnCameraMoveListener { updateMarker() }

        fusedLocationClient.lastLocation.addOnSuccessListener {
            it?.let {
                fragmentViewModel.setLocation(it.latitude, it.longitude)
                initMap(it)
            }
        }
    }

    private fun updateMarker() {
        val target = map.cameraPosition.target
        fragmentViewModel.setLocation(target.latitude, target.longitude)
        marker.position = target
    }

    private fun onButtonClicked(buttonId: Int) {
        hideKeyboard()
        when (buttonId) {
            R.id.btn_next -> {
                onNextListener.onNextClicked()
            }
        }
    }
}
