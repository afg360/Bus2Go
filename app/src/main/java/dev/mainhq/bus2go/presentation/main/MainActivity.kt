package dev.mainhq.bus2go.presentation.main

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.databinding.MainActivityBinding
//import dev.mainhq.bus2go.fragments.alarms.AlarmReceiver
import dev.mainhq.bus2go.presentation.main.home.HomeFragment
import dev.mainhq.bus2go.utils.launchViewModelCollect
import dev.mainhq.bus2go.utils.makeGone
import dev.mainhq.bus2go.utils.makeVisible
import dev.mainhq.bus2go.utils.toEpochDay
import dev.mainhq.bus2go.utils.toEpochMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate


//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)


class MainActivity : BaseActivity() {

    private val mainActivityViewModel: MainActivityViewModel by viewModels{
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return (this@MainActivity.application as Bus2GoApplication).let{
                    MainActivityViewModel(
                        checkDatabaseUpdateRequired = it.commonModule.checkDatabaseUpdateRequired,
                        wasUpdateDialogShownToday = it.commonModule.wasUpdateDialogShownToday,
                        setUpdateDbDialogLastAsToday = it.commonModule.setUpdateDbDialogLastAsToday,
                        setDatabaseExpirationDate = it.commonModule.setDatabaseExpirationDate,
                        getSettings = it.commonModule.getSettings,
                        checkIsBus2GoServer = it.appModule.checkIsBus2GoServer,
                        scheduleDownloadDatabaseTask = it.appModule.scheduleDownloadDatabaseTask
                    ) as T
                }
            }
        }
    }

    //private val alarmViewModel: AlarmCreationViewModel by viewModels()
    private lateinit var binding: MainActivityBinding

	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //TODO check if need to start configuration activity here
        checkAndUpdateDatabases()
        showDbNeedsUpdateTopView()

        launchViewModelCollect(mainActivityViewModel.updateDbState) {
            when(it){
                is UpdateDbState.Error -> {
                    Toast.makeText(this@MainActivity, "Some shitty error", Toast.LENGTH_SHORT)
                        .show()
                }
                UpdateDbState.NoShow -> {}
                UpdateDbState.NotConnectedToInternet -> {
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "Please connect to the internet", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                is UpdateDbState.Show -> {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Downloading shit...")
                }
            }
        }

        launchViewModelCollect(mainActivityViewModel.activityFragment) { activityType ->
            when (activityType) {
                ActivityFragment.HOME -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFragmentContainer, HomeFragment())
                        .commit()
                }

                ActivityFragment.MAP -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFragmentContainer, ComingSoonFragment())
                        .commit()
                }

                ActivityFragment.ALARMS -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFragmentContainer, ComingSoonFragment())
                        .commit()
                }
            }
        }

        binding.bottomNavBarView.setOnItemSelectedListener {
            //we change the state. Since the ui has an "observer", changes ui accordingly
            when(it.itemId) {
                R.id.homeScreenButton -> {
                    mainActivityViewModel.setActivityType(ActivityFragment.HOME)
                    true
                }
                R.id.mapButton -> {
                    mainActivityViewModel.setActivityType(ActivityFragment.MAP)
                    true
                }
                R.id.alarmsButton -> {
                    mainActivityViewModel.setActivityType(ActivityFragment.ALARMS)
                    true
                }
                else -> false
            }
        }
    }

    //TODO add some classes in data/domain layer handling this
    private fun checkAndUpdateDatabases() {
        launchViewModelCollect(mainActivityViewModel.showUpdateDbDialog) { showUpdateDbDialog ->
            if (showUpdateDbDialog) {
                // Displays a dialog for the user to choose to update now or to get reminded later.
                withContext(Dispatchers.Main) {
                    //Notes: .cancel is the same as .dismiss, but also calls the cancelListener
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Update your databases")
                        .setMessage(
                            "Local Bus2Go databases are out of date. " +
                                    "Update them now to enjoy accurate schedules."
                        )
                        .setPositiveButton("Update now") { dialogInterface, _ ->
                            //TODO setup download jobs and shit, no server prepared yet so display a coming soon for now
                            mainActivityViewModel.updateDatabase()
                            MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle("Coming Soon")
                                .setMessage(
                                    "Unfortunately, we are not hosting the dbs at the moment. " +
                                            "Please update the app when an update will be available."
                                )
                            //.show()
                            //dialogInterface.dismiss()
                            //mainActivityViewModel.setUpdateDbExpirationDate(30)
                        }
                        .setNeutralButton("Remind me later") { dialogInterface, _ ->
                            //TODO save the value in the application_state file (create a new dialog for choosing time before a reminder)
                            val datePicker = MaterialDatePicker.Builder.datePicker()
                                .setTitleText("Remind me in...")
                                .setCalendarConstraints(
                                    CalendarConstraints.Builder()
                                        .setValidator(
                                            CompositeDateValidator.allOf(
                                                listOf(
                                                    DateValidatorPointForward.from(
                                                        LocalDate.now()
                                                            //TODO For debugging, set it to today
                                                            // on release, + 1
                                                            //.plusDays(1)
                                                            .toEpochMillis()
                                                    ),
                                                    DateValidatorPointBackward.before(
                                                        LocalDate.now()
                                                            .plusDays(60L)
                                                            .toEpochMillis()
                                                    )
                                                )
                                            )
                                        )
                                        .build()
                                )
                                .setPositiveButtonText("Confirm")
                                .setNegativeButtonText("Cancel")
                                .build()
                            datePicker.addOnPositiveButtonClickListener {
                                mainActivityViewModel.setUpdateDbExpirationDate(it.toEpochDay())
                                dialogInterface.dismiss()
                            }
                            datePicker.addOnNegativeButtonClickListener {
                                dialogInterface.dismiss()
                            }
                            datePicker.show(this@MainActivity.supportFragmentManager, null)
                        }
                        .setNegativeButton("Don't remind me") { dialogInterface, _ ->
                            mainActivityViewModel.setUpdateDbExpirationDate(30)
                            dialogInterface.cancel()
                        }
                        .create()
                        .show()
                }
            }
        }
    }

    private fun showDbNeedsUpdateTopView(){
        launchViewModelCollect(mainActivityViewModel.showUpdateTextView) {
            if (it){
                binding.mainDbNeedsUpdateTextView.makeVisible()
                binding.mainDbNeedsUpdateImageView.makeVisible()
            }
            else {
                binding.mainDbNeedsUpdateTextView.makeGone()
                binding.mainDbNeedsUpdateImageView.makeGone()
            }
        }
    }

    /*
    fun setAlarm(context: Context, calendar: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
    }
     */
}