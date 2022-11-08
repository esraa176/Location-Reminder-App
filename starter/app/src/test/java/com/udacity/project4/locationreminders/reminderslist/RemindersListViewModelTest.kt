package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var remindersRepository: FakeDataSource
    private lateinit var appContext: Application

    private val fakeRemindersList = mutableListOf(
        ReminderDTO("reminder 1", "description", "location", 100.00, 100.00),
        ReminderDTO("reminder 2", "description", "location", 200.00, 200.00),
        ReminderDTO("reminder 3", "description", "location", 300.00, 300.00),
        ReminderDTO("reminder 4", "description", "location", 400.00, 400.00)
    )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
        remindersRepository = FakeDataSource(fakeRemindersList)
        viewModel = RemindersListViewModel(appContext, remindersRepository)
    }

    @Test
    fun validateLoadReminders() {
        viewModel.loadReminders()

        val expectedResult = fakeRemindersList.map {
            it.toReminderDataItem()
        }

        assertThat(viewModel.remindersList.getOrAwaitValue(), `is`(expectedResult))
    }

    @Test
    fun loadReminders_returnError(){
        remindersRepository.setReturnError(true)

        viewModel.loadReminders()

        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Error retrieving reminders"))
    }

    @Test
    fun remindersList_checkLoading() = mainCoroutineRule.runBlockingTest {
        // GIVEN
        val fakeReminder = ReminderDataItem("reminder", "description", "location", 100.00, 100.00)
        mainCoroutineRule.pauseDispatcher()

        // WHEN
        viewModel.loadReminders()

        // THEN
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}