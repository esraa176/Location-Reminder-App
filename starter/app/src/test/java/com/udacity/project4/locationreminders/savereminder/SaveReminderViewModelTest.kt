package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import org.hamcrest.MatcherAssert.assertThat

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var remindersRepository: FakeDataSource
    private lateinit var appContext: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
        remindersRepository = FakeDataSource()
        viewModel = SaveReminderViewModel(appContext, remindersRepository)
    }

    @Test
    fun createReminder_validateSaveReminder() {
        // GIVEN
        val fakeReminder = ReminderDataItem("reminder", "description", "location", 100.00, 100.00)

        // WHEN
        viewModel.saveReminder(fakeReminder)

        // THEN
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`(appContext.getString(R.string.reminder_saved)))
        assertThat(viewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.Back))
    }

    @Test
    fun saveReminder_checkLoading() = mainCoroutineRule.runBlockingTest {
        // GIVEN
        val fakeReminder = ReminderDataItem("reminder", "description", "location", 100.00, 100.00)
        mainCoroutineRule.pauseDispatcher()
        viewModel.reminderDataItem.value = fakeReminder

        // WHEN
        viewModel.saveReminder(fakeReminder)

        // THEN
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}