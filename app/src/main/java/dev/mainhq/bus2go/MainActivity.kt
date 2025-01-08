package dev.mainhq.bus2go

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationBarView
//import dev.mainhq.bus2go.fragments.alarms.AlarmReceiver
import dev.mainhq.bus2go.fragments.ComingSoon
import dev.mainhq.bus2go.fragments.Home
import dev.mainhq.bus2go.viewmodels.AlarmCreationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar


//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)

val Context.firstTimeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "application_state.json"
)

class MainActivity : BaseActivity() {

    private lateinit var activityType : ActivityType

	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //FIXME only for testing dont run the whole code
        /*
        val first = intent.getBooleanExtra("first", true)
        if (first) {
            val intent = Intent(applicationContext, ConfigActivity::class.java)
            finish()
            startActivity(intent)
        }
        else
            setupActivity()
        */
        setupActivity()
        /*
        //TODO check if need to start configuration activity here
        lifecycleScope.launch(Dispatchers.IO) {
            if (isFirstTime()){
                withContext(Dispatchers.Main){
                    val intent = Intent(applicationContext, ConfigActivity::class.java)
                    /** Once the configuration is done, we will automatically start the MainActivity */
                    startActivity(intent)
                }
            }
            withContext(Dispatchers.Main){
            }
        }
         */

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

    /** To check if first time opening the app, check for the existence of the PreferenceManager field
     * 	If false/doesn't exist, then first time.
     * 	However, for long time users, check if the databases exist. If they don't, then we are sure
     * 	it is their first time */
    private suspend fun isFirstTime(): Boolean{
        val keyName = booleanPreferencesKey("isFirstTime")
        if  (firstTimeDataStore.data.first().contains(keyName)){
            return firstTimeDataStore.data.first()[keyName] ?: true
        }
        else{
            //TODO check for the existence of a bus2go database folder/files
            val directory = File(filesDir, "database")
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

