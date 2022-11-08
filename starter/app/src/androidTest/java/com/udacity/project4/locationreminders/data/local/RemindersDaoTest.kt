package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersDao: RemindersDao

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDB(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()

        remindersDao = database.reminderDao()
    }

    @After
    fun closeDB(){
        database.close()
    }

    @Test
    fun saveReminder_getReminderId() = runBlocking {
        // GIVEN
        val fakeReminder = ReminderDTO("reminder 1","description","location", 100.0, 100.0)
        remindersDao.saveReminder(fakeReminder)

        // WHEN
        val result = remindersDao.getReminderById(fakeReminder.id)

        // THEN
        assertThat(result, notNullValue())
        assertThat(result?.id, `is`(fakeReminder.id))
        assertThat(result?.title, `is`(fakeReminder.title))
        assertThat(result?.description, `is`(fakeReminder.description))
        assertThat(result?.location, `is`(fakeReminder.location))
        assertThat(result?.latitude, `is`(fakeReminder.latitude))
        assertThat(result?.longitude, `is`(fakeReminder.longitude))
    }

    @Test
    fun givenUnsavedReminder_returnNoReminder() = runBlocking {
        // GIVEN
        val fakeReminderId = "fake reminder"

        // WHEN
        val result = remindersDao.getReminderById(fakeReminderId)

        // THEN
        assertThat(result, nullValue())
    }
}