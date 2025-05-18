package dev.mainhq.bus2go.presentation.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
//import dev.mainhq.bus2go.fragments.alarms.AlarmReceiver
import dev.mainhq.bus2go.presentation.main.home.HomeFragment
import dev.mainhq.bus2go.presentation.utils.ActivityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)

class MainActivity : BaseActivity() {

    private val mainActivityViewModel: MainActivityViewModel by viewModels{
        object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainActivityViewModel(
                    (this@MainActivity.application as Bus2GoApplication).commonModule.checkDatabaseUpdateRequired,
                    (this@MainActivity.application as Bus2GoApplication).commonModule.setDatabaseState,
                ) as T
            }
        }
    }

    //private val alarmViewModel: AlarmCreationViewModel by viewModels()
    //private val roomViewModel: RoomViewModel by viewModels()


	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        //if (AppThemeState.displayIsDbUpdatedDialog)
        //    checkAndUpdateDatabases()

        findViewById<NavigationBarView>(R.id.bottomNavBarView).setOnItemSelectedListener {
            //we change the state. Since the ui has an "observer", changes ui accordingly
            when(it.itemId) {
                R.id.homeScreenButton -> {
                    mainActivityViewModel.setActivityType(ActivityType.HOME)
                    true
                }
                R.id.mapButton -> {
                    mainActivityViewModel.setActivityType(ActivityType.MAP)
                    true
                }
                R.id.alarmsButton -> {
                    mainActivityViewModel.setActivityType(ActivityType.ALARMS)
                    true
                }
                else -> false
            }
        }


        //TODO check if need to start configuration activity here
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                mainActivityViewModel.activityType.collect{ activityType ->
                    when(activityType){
                        ActivityType.HOME -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.mainFragmentContainer, HomeFragment())
                                .commit()
                        }
                        ActivityType.MAP -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.mainFragmentContainer, ComingSoonFragment())
                                .commit()
                        }
                        ActivityType.ALARMS -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.mainFragmentContainer, ComingSoonFragment())
                                .commit()
                        }
                    }
                }
            }
        }
    }

    //TODO add some classes in data/domain layer handling this
    private suspend fun checkAndUpdateDatabases(){
        mainActivityViewModel.showAlert.collect{ showAlert ->
            if (showAlert){
                // Displays a dialog for the user to choose to update now or to get reminded later.
                withContext(Dispatchers.Main) {
                    val dialog = MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Update your databases")
                        .setMessage(
                            "It seems like your local databases are out of date. It is recommended that you " +
                                    "update them so that you can enjoy accurate schedules."
                        )
                        .setPositiveButton("Update now") { dialogInterface, _ ->
                            //TODO setup download jobs and shit, no server prepared yet so display a coming soon for now
                            MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle("Coming Soon")
                                .setMessage("Unfortunately, we are not hosting the dbs at the moment. Please update" +
                                        "the app when an update will be available.")
                                .show()
                            dialogInterface.dismiss()
                            mainActivityViewModel.setUpdateDbState(30)
                        }
                        .setNeutralButton("Remind me later") { dialogInterface, _ ->
                            //TODO save the value in the application_state file (create a new dialog for choosing time before a reminder)
                            val datePicker = MaterialDatePicker.Builder.datePicker()
                                .setTitleText("Remind me in...")
                                .setPositiveButtonText("Confirm")
                                .setNegativeButtonText("Cancel")
                                .build()
                            datePicker.addOnPositiveButtonClickListener {
                                mainActivityViewModel.setUpdateDbState(it)
                                dialogInterface.dismiss()
                            }
                            datePicker.addOnNegativeButtonClickListener {
                                dialogInterface.dismiss()
                            }
                            datePicker.show(this@MainActivity.supportFragmentManager, null)
                        }
                        .setNegativeButton("Don't remind me") { dialogInterface, _ ->
                            mainActivityViewModel.setUpdateDbState(30)
                            dialogInterface.cancel()
                        }
                        .create()

                    dialog.show()
                }
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