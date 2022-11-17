package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    lateinit var geofencingClient: GeofencingClient

    private val backgroundLocationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
            if (isPermissionGranted) {
                addGeofence()
            } else {
                _viewModel.showSnackBarInt.value = R.string.location_denied
            }
        }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            navigateToSelectLocation()
        }

        binding.saveReminder.setOnClickListener {
            if (_viewModel.validateEnteredData()) {
                requestPermissionAndAddGeofence()
            } else {
                _viewModel.showSnackBarInt.value = R.string.missing_data_error
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun checkBackgroundLocationPermission() {
        val locationPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && shouldShowRequestPermissionRationale(locationPermission)
        ) {
            showLocationPermissionsDialog()
        } else {
            requestBackgroundLocationPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission(){
        backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showLocationPermissionsDialog() {
        val dialogMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getString(R.string.permission_bg_rationale_with_label, requireContext().packageManager.backgroundPermissionOptionLabel)
        } else {
            getString(R.string.permission_bg_rationale)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_required_error)
            .setMessage(dialogMessage)
            .setPositiveButton("OK") { _, _ ->
                requestBackgroundLocationPermission()
            }
            .show()
    }

    private fun navigateToSelectLocation() {
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        val reminderDataItem = _viewModel.reminderDataItem.value
        if (reminderDataItem == null) {
            _viewModel.showSnackBarInt.value = R.string.pick_location
        } else {
            _viewModel.saveReminder(reminderDataItem)
            val geofencingList = createGeofence(reminderDataItem)
            geofencingClient.addGeofences(
                getGeofencingRequest(geofencingList),
                geofencePendingIntent
            ).run {
                addOnSuccessListener {
                    _viewModel.showSnackBarInt.value = R.string.reminder_saved
                }
                addOnFailureListener {
                    // Failed to add geofence
                    _viewModel.showSnackBarInt.value = R.string.error_adding_geofence
                }
            }
        }
    }

    private fun requestPermissionAndAddGeofence() {
        val reminderDataItem = _viewModel.reminderDataItem.value
        if (reminderDataItem == null) {
            _viewModel.showSnackBarInt.value = R.string.pick_location
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    checkBackgroundLocationPermission()
                } else {
                    addGeofence()
                }
            } else {
                addGeofence()
            }
        }
    }

    private fun getGeofencingRequest(geofenceList: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private fun createGeofence(reminder: ReminderDataItem): List<Geofence> {
        val geofenceList = mutableListOf<Geofence>()
        geofenceList.add(
            Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(reminder.id)

                // Set the circular region of this geofence.
                .setCircularRegion(
                    reminder.latitude!!,
                    reminder.longitude!!,
                    GEOFENCE_RADIUS_IN_METERS
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                // Create the geofence.
                .build()
        )
        return geofenceList
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()

    }

    companion object {
        private const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
        private const val GEOFENCE_RADIUS_IN_METERS = 1609f // 1 mile, 1.6 km
    }
}