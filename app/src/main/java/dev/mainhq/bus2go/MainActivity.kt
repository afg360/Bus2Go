package dev.mainhq.bus2go

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
//import dev.mainhq.bus2go.fragments.alarms.AlarmReceiver
import dev.mainhq.bus2go.fragments.ComingSoon
import dev.mainhq.bus2go.fragments.Home
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.viewmodels.AlarmCreationViewModel
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)

val Context.applicationStateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "application_state"
)

class MainActivity : BaseActivity() {

    private lateinit var activityType : ActivityType
    private val roomViewModel: RoomViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO check if need to start configuration activity here
        lifecycleScope.launch(Dispatchers.IO) {
            if (isFirstTime()){
                withContext(Dispatchers.Main){
                    val intent = Intent(applicationContext, ConfigActivity::class.java)
                    /** Once the configuration is done, we will automatically start the MainActivity */
                    startActivity(intent)
                    //AppThemeState.turnOffDbUpdateChecking()
                }
            }

            if (AppThemeState.displayIsDbUpdatedDialog)
                checkAndUpdateDatabases()

            withContext(Dispatchers.Main){
                setupActivity()
            }
        }
    }

    private fun setupActivity(){
        setContentView(R.layout.main_activity)

        activityType = ActivityType.HOME
        val home = Home()
        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, home).commit()

        val alarmViewModel = ViewModelProvider(this)[AlarmCreationViewModel::class.java]
        val bottomNav = findViewById<NavigationBarView>(R.id.bottomNavBarView)
        bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.homeScreenButton -> {
                    // Respond to navigation item 1 click
                    if (activityType != ActivityType.HOME) {
                        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer,
                            Home()).commit()
                        activityType = ActivityType.HOME
                    }
                    true
                }
                R.id.mapButton -> {
                    // Respond to navigation item 2 click
                    if (activityType != ActivityType.MAP) {
                        //supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, Map()).commit()
                        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, ComingSoon()).commit()
                        activityType = ActivityType.MAP
                    }
                    true
                }
                R.id.alarmsButton -> {
                    // Respond to navigation item 2 click
                    if (activityType != ActivityType.ALARMS) {
                        //supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer,
                        //    Alarms(alarmViewModel, favouritesViewModel)).commit()
                        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, ComingSoon()).commit()
                        activityType = ActivityType.ALARMS
                    }
                    true
                }
                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                //Toast.makeText(this@MainActivity, "First back", Toast.LENGTH_SHORT).show()
                if (activityType == ActivityType.HOME) {
                    home.onBackPressed()
                }
            }
        })
    }

    private suspend fun checkAndUpdateDatabases(){
        val databaseStateKey = stringPreferencesKey("databases_state")
        if (!applicationStateDataStore.data.first().contains(databaseStateKey)){
            //FIXME instead of checking both dbs at the same time, check each individually to only download
            //the required ones
            val timeForUpdate = roomViewModel.getMinDateForUpdate()
            if  (timeForUpdate == null){
                displayUpdateDatabasesDialog()?.also{ time ->
                    applicationStateDataStore.edit { mutablePreferences ->
                        mutablePreferences[databaseStateKey] = Time.toLocalDateString(LocalDate.now().plusDays(time))
                    }
                }
                AppThemeState.turnOffDbUpdateChecking()
            }
            else{
                applicationStateDataStore.edit { mutablePreferences ->
                    mutablePreferences[databaseStateKey] = Time.toLocalDateString(timeForUpdate)
                }
                AppThemeState.turnOffDbUpdateChecking()
            }
        }
        else {
            val savedDate = LocalDate.parse(applicationStateDataStore.data.first()[databaseStateKey], DateTimeFormatter.BASIC_ISO_DATE)
            if (savedDate < LocalDate.now()){
                displayUpdateDatabasesDialog()?.also{ time ->
                    applicationStateDataStore.edit { mutablePreferences ->
                        mutablePreferences[databaseStateKey] = Time.toLocalDateString(LocalDate.now().plusDays(time))
                    }
                }
                AppThemeState.turnOffDbUpdateChecking()
            }
        }
    }

    /**
     * Displays a dialog for the user to choose to update now or to get reminded later.
     * @return The number of days before displaying a new notification and dialog. Null if update
     * occurs.
     **/
    private suspend fun displayUpdateDatabasesDialog(): Long? {
        //TODO Add notification to task bar
        return withContext(Dispatchers.Main) {
            val deferred = CompletableDeferred<Long?>()
            val dialog = MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("Update your databases")
                .setMessage(
                    "It seems like your local databases are out of date. It is recommended that you " +
                            "update them so that you can enjoy accurate schedules."
                )
                .setPositiveButton("Update now") { dialogInterface, _ ->
                    //TODO setup download jobs and shit
                    dialogInterface.dismiss()
                    deferred.complete(null)
                }
                .setNeutralButton("Remind me later") { dialogInterface, _ ->
                    //TODO save the value in the application_state file (create a new dialog for choosing time before a reminder)
                    val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Remind me in...")
                        .setPositiveButtonText("Confirm")
                        .setNegativeButtonText("Cancel")
                        .build()
                    datePicker.addOnPositiveButtonClickListener {
                        deferred.complete(it)
                        dialogInterface.dismiss()
                    }
                    datePicker.addOnNegativeButtonClickListener {
                        deferred.complete(null)
                        dialogInterface.dismiss()
                    }
                    datePicker.show(this@MainActivity.supportFragmentManager, null)
                }
                .setNegativeButton("Don't remind me") { dialogInterface, _ ->
                    deferred.complete(30)
                    dialogInterface.cancel()
                }
                .create()

            dialog.show()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) dialog.dismiss()
            }

            deferred.await()
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

    /**
     * To check if first time opening the app, check for the existence of the PreferenceManager field
     * If false/doesn't exist, then first time.
     * However, for long time users, check if the databases exist. If they don't, then we are sure
     * it is their first time
     **/
    private suspend fun isFirstTime(): Boolean{
        val keyName = booleanPreferencesKey("isFirstTime")
        if  (applicationStateDataStore.data.first().contains(keyName)){
            return applicationStateDataStore.data.first()[keyName] ?: true
        }
        else{
            //TODO check for the existence of a bus2go database folder/files
            val directory = File(dataDir, "databases")
            if (directory.exists() && directory.isDirectory){
                return directory.list()?.isEmpty() ?: true
            }
            return true
        }
    }

    private enum class ActivityType{
        HOME, MAP, ALARMS
    }
}

