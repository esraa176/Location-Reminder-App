package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDB(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()

        val remindersDao = database.reminderDao()
        remindersLocalRepository = RemindersLocalRepository(remindersDao, Dispatchers.Unconfined)
    }

    @After
    fun closeDB(){
        database.close()
    }

    @Test
    fun saveReminder_getReminderId() = runBlocking {
        // GIVEN
        val fakeReminder = ReminderDTO("reminder 1","description","location", 100.0, 100.0)
        remindersLocalRepository.saveReminder(fakeReminder)

        // WHEN
        val result = remindersLocalRepository.getReminder(fakeReminder.id)

        // THEN
        assertThat(result, `is`(Result.Success(fakeReminder)))
    }

    @Test
    fun givenUnsavedReminder_returnNoReminder() = runBlocking {
        // GIVEN
        val fakeReminderId = "fake reminder"

        // WHEN
        val result = remindersLocalRepository.getReminder(fakeReminderId)

        // THEN
        assertThat(result, `is`(Result.Error("Reminder not found!")))
    }
}