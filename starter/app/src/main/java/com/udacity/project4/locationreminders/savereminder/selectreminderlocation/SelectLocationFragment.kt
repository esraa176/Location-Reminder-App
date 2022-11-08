package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.round
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var googleMap: GoogleMap
    private var marker: Marker? = null

    private val locationSettingsRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            showEnableLocationSetting(
                false
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        showEnableLocationSetting()

        binding.saveLocation.setOnClickListener {
            if (marker == null) {
                _viewModel.showSnackBarInt.value = R.string.err_select_location
            } else {
                _viewModel.navigationCommand.value = NavigationCommand.Back
            }
        }
        return binding.root
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(100)
            .setMaxUpdateDelayMillis(1000)
            .setMaxUpdates(1)
            .build()
    }

    private fun showEnableLocationSetting(resolve: Boolean = true) {
        val locationRequest = getLocationRequest()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val task = LocationServices.getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getDeviceLocation()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException && resolve) {
                try {
                    locationSettingsRequestLauncher.launch(
                        IntentSenderRequest.Builder(e.resolution.intentSender).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                _viewModel.showSnackBarInt.value = R.string.location_required_error
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                // Set the map's camera position to the current location of the device.
                val lastKnownLocation = task.result
                if (lastKnownLocation != null) {
                    animateGoogleMapsCamera(lastKnownLocation)
                } else {
                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val lastLocation = locationResult.lastLocation
                            if (lastLocation != null) {
                                animateGoogleMapsCamera(lastLocation)
                            } else {
                                Log.d(TAG, "Failed to get current user's location")
                            }
                        }
                    }
                    fusedLocationProviderClient.requestLocationUpdates(
                        getLocationRequest(),
                        locationCallback,
                        Looper.myLooper()
                    )
                }
            } else {
                googleMap.uiSettings.isMyLocationButtonEnabled = false
            }
        }
    }

    private fun animateGoogleMapsCamera(location: Location) {
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), DEFAULT_ZOOM.toFloat()
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style
            )
        )

        googleMap.isMyLocationEnabled = true

        googleMap.setOnMapClickListener {
            val locationName = "${it.latitude.round()}, ${it.longitude.round()}"
            marker?.remove()
            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(locationName)
            )
            _viewModel.updateLocation(it)
        }

        googleMap.setOnPoiClickListener {
            marker?.remove()
            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
            )
            _viewModel.updateLocation(it.latLng, it.name)
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
        private val TAG = SelectLocationFragment::class.simpleName
    }
}
