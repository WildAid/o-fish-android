package org.wildaid.ofish.ui.basicinformation

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.mpa.addTestMpa
import org.wildaid.ofish.databinding.FragmentBasicInformationBinding
import org.wildaid.ofish.ui.base.BaseReportFragment
import org.wildaid.ofish.ui.home.ZOOM_LEVEL
import org.wildaid.ofish.util.getViewModelFactory
import org.wildaid.ofish.util.hideKeyboard
import org.wildaid.ofish.util.showManuallySelectedLocationDialog
import java.util.*

const val LONG = 0
const val LAT = 1


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

    override fun isAllRequiredFieldsNotEmpty(): Boolean {
        return true
    }

    override fun validateForms(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewDataBinding = FragmentBasicInformationBinding.bind(view).apply {
            this.viewmodel = fragmentViewModel
            this.lifecycleOwner = this@BasicInformationFragment.viewLifecycleOwner
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.basic_info_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fragmentViewModel.basicInfoUserEventLiveData.observe(
            viewLifecycleOwner, EventObserver(::handleUserEvent)
        )

        viewDataBinding.locationLayout.setOnClickListener { dialogSetup() }
    }

    @SuppressLint("MissingPermission")
    private fun initMap(location: Location) {
        val coord = LatLng(location.latitude, location.longitude)
        if (!::marker.isInitialized) {
            marker = map.addMarker(
                MarkerOptions()
                    .position(coord)
            )
        }
        map.isMyLocationEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, ZOOM_LEVEL))
    }

    private fun subscribeToNavigationResult() {
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        this.map = map

        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (isNightMode == Configuration.UI_MODE_NIGHT_YES)
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    activity?.applicationContext,
                    R.raw.map_dark_mode
                )
            )

        if (currentReport.draft == true) {
            val long = currentReport.location[LONG].toString()
            val lat = currentReport.location[LAT].toString()

            val location = Location("")
            location.latitude = lat.toDouble()
            location.longitude = long.toDouble()

            fragmentViewModel.setLocation(location.latitude, location.longitude)
            initMap(location)
            cameraMoveListener()
        } else {
            if (!isLocationTurnedOn(requireContext()))
                setDummyLocation()
            cameraMoveListener()

            fusedLocationClient.lastLocation.addOnSuccessListener {
                it?.let {
                    fragmentViewModel.setLocation(it.latitude, it.longitude)
                    initMap(it)
                }
            }
            addTestMpa(this.map, resources)
        }

    }

    private fun cameraMoveListener() {
        this.map.setOnCameraMoveListener {
            updateMarker()
        }
    }

    private fun updateMarker() {
        val target = map.cameraPosition.target
        fragmentViewModel.setLocation(target.latitude, target.longitude)
        marker.position = target
    }

    private fun handleUserEvent(event: BasicInformationViewModel.BasicInfoUserEvent) {
        hideKeyboard()
        when (event) {
            BasicInformationViewModel.BasicInfoUserEvent.NextEvent -> {
                onNextListener.onNextClicked()
            }
            BasicInformationViewModel.BasicInfoUserEvent.ChooseDate -> {
                peekDate()
            }
            BasicInformationViewModel.BasicInfoUserEvent.ChooseTime -> {
                peekTime()
            }
        }
    }

    private fun peekDate() {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val dialog = DatePickerDialog(
            requireContext(), ::onDatePicked,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun onDatePicked(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        fragmentViewModel.updateDate(year, month, dayOfMonth)
    }

    private fun peekTime() {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
        val dialog = TimePickerDialog(
            requireContext(),
            ::onTimePicked,
            calendar.get(Calendar.HOUR),
            calendar.get(Calendar.MINUTE),
            false
        )
        dialog.show()
    }

    private fun onTimePicked(timePicker: TimePicker, hourOfDay: Int, minute: Int) {
        fragmentViewModel.updateTime(hourOfDay, minute)
    }

    private fun isLocationTurnedOn(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun setDummyLocation() {
        val dummyLocation = LocationManager.GPS_PROVIDER
        val loc = Location(dummyLocation)
        val mockLocation = Location(dummyLocation)
        mockLocation.latitude = 31.398667
        mockLocation.longitude = -99.31185  //Texas
        mockLocation.altitude = loc.altitude
        mockLocation.time = System.currentTimeMillis()
        mockLocation.accuracy = 1f

        initMap(mockLocation)

    }

    private fun dialogSetup() {
        showManuallySelectedLocationDialog(
            viewDataBinding.basicInfoLat,
            viewDataBinding.basicInfoLong
        ) { onManuallySelectedLocationSuccess() }
    }

    private fun onManuallySelectedLocationSuccess() {
        val latitude = viewDataBinding.basicInfoLat.text.toString().toDouble()
        val longitude = viewDataBinding.basicInfoLong.text.toString().toDouble()

        val location = Location("")
        location.latitude = latitude
        location.longitude = longitude

        fragmentViewModel.setLocation(latitude, longitude)
        initMap(location)
        updateMarker()

    }

}