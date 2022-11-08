package com.udacity.project4.locationreminders.reminderslist

import android.os.Parcelable
import com.udacity.project4.utils.round
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
@Parcelize
data class ReminderDataItem(
    var title: String = "",
    var description: String = "",
    var location: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    val id: String = UUID.randomUUID().toString()
) : Parcelable {
    val locationName: String?
        get() {
            if (location != null) {
                return location
            }
            if (latitude != null && longitude != null) {
                return "${latitude!!.round()}, ${longitude!!.round()}"
            }
            return null
        }
}