package org.wildaid.ofish.ui.home

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_home.*
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.data.mpa.addTestMpa
import org.wildaid.ofish.databinding.FragmentHomeBinding
import org.wildaid.ofish.ui.base.*
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.ui.search.complex.ComplexSearchFragment
import org.wildaid.ofish.util.*

const val KEY_CREATE_REPORT_RESULT = "create_report_message_result"
const val ZOOM_LEVEL = 10f

private const val REQUEST_CODE_PERMISSIONS = 1444
private const val CREATE_REPORT_FINISHED_DIALOG_ID = 100

class HomeFragment : Fragment(R.layout.fragment_home),
    OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var androidPermissions: AndroidPermissions
    private lateinit var dataBinding: FragmentHomeBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var pendingImageUri: Uri

    private val navigation: NavController by lazy { findNavController() }
    private val fragmentViewModel: HomeFragmentViewModel by viewModels { getViewModelFactory() }
    private val activityViewModel: HomeActivityViewModel by activityViewModels { getViewModelFactory() }
    private val requiredPermissions = arrayOf(ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidPermissions = AndroidPermissions(requireActivity(), this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fragmentViewModel.activityViewModel = activityViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI(view)

        setObservers()
        subscribeToDialogEvents()
        arguments?.let {
            if (it.containsKey(KEY_CREATE_REPORT_RESULT)) {
                val message = it.getString(KEY_CREATE_REPORT_RESULT, null)
                it.remove(KEY_CREATE_REPORT_RESULT)
                showCreateReportDialog(message)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentViewModel.updateDraftCount()
    }

    private fun setObservers() {
        fragmentViewModel.locationLiveData.observe(viewLifecycleOwner, Observer {
            home_latitude.text = convert(it.first, LATITUDE)
            home_longitude.text = convert(it.second, LONGITUDE)
        })

        fragmentViewModel.userEventLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                HomeFragmentViewModel.HomeFragmentUserEvent.BoardVessel -> boardVessel()
                HomeFragmentViewModel.HomeFragmentUserEvent.FindRecords -> findRecords()
                HomeFragmentViewModel.HomeFragmentUserEvent.ShowUserStatus -> navigateToProfile()
            }
        })

        fragmentViewModel.amountOfDrafts.observe(viewLifecycleOwner, Observer { amount ->
            if (amount > 0) {
                showAmountLabel()
                text_amount_of_drafts.text = amount.toString()
            } else {
                hideAmountLabel()
            }
        })

        activityViewModel.currentOfficerLiveData.observe(viewLifecycleOwner, Observer {
            showOfficerPhoto(image_user)
        })

        activityViewModel.onDutyStatusLiveData.observe(viewLifecycleOwner, Observer { dutyStatus ->
            image_user_status.isEnabled = dutyStatus
        })
    }

    private fun hideAmountLabel() {
        text_amount_of_drafts.visibility = View.GONE
        image_amount_drafts.visibility = View.GONE
    }

    private fun showAmountLabel() {
        text_amount_of_drafts.visibility = View.VISIBLE
        image_amount_drafts.visibility = View.VISIBLE
    }

    private fun navigateToProfile() {
        navigation.navigate(R.id.action_home_fragment_to_profileFragment)
    }

    private fun showOfficerPhoto(view: ImageView?) {
        view?.let {
            val photo = fragmentViewModel.repository.getCurrentOfficerPhoto()
            Glide.with(this)
                .load(photo?.getResourceForLoading())
                .placeholder(R.drawable.ic_account_circle)
                .into(it)
        }
    }

    private fun updateProfilePhoto() {
        Glide.with(this)
            .clear(image_user)

        Glide.get(requireContext()).clearMemory()
        Thread {
            Glide.get(requireContext()).clearDiskCache()
        }.start()

        showOfficerPhoto(image_user)
    }

    private fun initUI(view: View) {
        dataBinding = FragmentHomeBinding.bind(view).apply {
            this.lifecycleOwner = viewLifecycleOwner
            this.fragmentViewModel = this@HomeFragment.fragmentViewModel
            this.homeActivityViewModel = this@HomeFragment.activityViewModel
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.home_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkPermissions()
        addTestMpa(googleMap, resources)
    }

    @SuppressLint("ResourceType")
    private fun setMyLocationPosition() {
        googleMap.isMyLocationEnabled = true
        val locationButton: View = (home_map.findViewById<View>(1).parent as View).findViewById(2)
        val rlp: RelativeLayout.LayoutParams =
            locationButton.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
        rlp.setMargins(
            0,
            resources.getDimension(R.dimen.my_location_margin_top).toInt(),
            resources.getDimension(R.dimen.my_location_margin_right).toInt(),
            0
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (androidPermissions.isPermissionsGranted(*requiredPermissions)) {
            onPermissionsGranted()
        } else {
            onPermissionsDeclined()
        }
    }

    private fun findRecords() {
        val bundle =
            bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to ComplexSearchFragment.SearchRecords)
        navigation.navigate(R.id.action_home_fragment_to_complex_search, bundle)
    }

    private fun boardVessel() {
        val bundle =
            bundleOf(BaseSearchFragment.SEARCH_ENTITY_KEY to ComplexSearchFragment.SearchBoardVessels)
        navigation.navigate(R.id.action_home_fragment_to_complex_search, bundle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: pendingImageUri
            fragmentViewModel.saveProfileImage(uri)
            updateProfilePhoto()
        }
    }

    private fun showCreateReportDialog(message: String) {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            CREATE_REPORT_FINISHED_DIALOG_ID,
            null,
            message,
            getString(android.R.string.ok)
        ).bundle()

        navigation.navigate(R.id.ask_change_duty_dialog, dialogBundle)
    }

    private fun checkPermissions() {
        val nonGranted = androidPermissions.checkNonGrantedPermissions(*requiredPermissions)

        if (nonGranted.isNotEmpty()) {
            androidPermissions.requestPermissions(REQUEST_CODE_PERMISSIONS, *nonGranted)
        } else {
            onPermissionsGranted()
        }
    }

    private fun onPermissionsDeclined() {
        requireActivity().finish()
    }

    @SuppressLint("MissingPermission")
    private fun onPermissionsGranted() {
        setMyLocationPosition()
        fusedLocationClient.lastLocation.addOnSuccessListener {
            it?.let {
                val coordinates = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, ZOOM_LEVEL))
                googleMap.isMyLocationEnabled = true
                fragmentViewModel.onLocationAvailable(it.latitude, it.longitude)
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
                if (handleDialogClick(click)) {
                    navStack.savedStateHandle.remove<DialogClickEvent>(DIALOG_CLICK_EVENT)!!
                }
            }
        })
    }

    private fun handleDialogClick(event: DialogClickEvent): Boolean {
        return when (event.dialogId) {
            ASK_CHANGE_DUTY_DIALOG_ID -> {
                if (event.dialogBtn == DialogButton.POSITIVE) {
                    activityViewModel.onDutyChanged(true)
                    boardVessel()
                }
                if (event.dialogBtn == DialogButton.NEGATIVE) {
                    activityViewModel.onDutyChanged(false)
                }
                true
            }
            else -> false
        }
    }
}