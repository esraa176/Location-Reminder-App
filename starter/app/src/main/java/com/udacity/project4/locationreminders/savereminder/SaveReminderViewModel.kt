package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderDataItem = MutableLiveData(ReminderDataItem())

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderDataItem.value = null
    }

    fun init() {
        reminderDataItem.value = ReminderDataItem()
    }

    fun updateLocation(location: LatLng, locationName: String? = null) {
        reminderDataItem.value?.latitude = location.latitude
        reminderDataItem.value?.longitude = location.longitude
        reminderDataItem.value?.location = locationName
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
//    fun validateAndSaveReminder(): Boolean {
//        val reminder = reminderDataItem.value!!
//        if (!validateEnteredData(reminder)) {
//            return false
//        }
//        saveReminder(reminder)
//        return true
//    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude!!,
                    reminderData.longitude!!,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(): Boolean {
        val reminderData = reminderDataItem.value!!
        if (reminderData.title.isEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.locationName.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}