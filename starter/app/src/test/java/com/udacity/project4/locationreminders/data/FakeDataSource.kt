package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //TODO("Return the reminders")

        val remindersList = listOf<ReminderDTO>(
            ReminderDTO("reminder 1","description","location", 100.0, 100.0) ,
            ReminderDTO("reminder 2","description","location", 200.0, 200.0) ,
            ReminderDTO("reminder 3","description","location", 300.0, 300.0) ,
            ReminderDTO("reminder 4","description","location", 400.0, 400.0)
        )
        return try {
            Result.Success(remindersList)
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        TODO("save the reminder")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO("return the reminder with the id")
    }

    override suspend fun deleteAllReminders() {
        TODO("delete all the reminders")
    }


}