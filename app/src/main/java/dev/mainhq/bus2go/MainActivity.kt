package dev.mainhq.bus2go

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationBarView
import dev.mainhq.bus2go.fragments.AlarmReceiver
import dev.mainhq.bus2go.fragments.ComingSoon
import dev.mainhq.bus2go.fragments.Home
import dev.mainhq.bus2go.viewmodels.AlarmCreationViewModel
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.launch
import java.util.Calendar


//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)

class MainActivity() : BaseActivity() {

    private lateinit var activityType : ActivityType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //if (false) { //TODO check if config file exists
        //    val intent = Intent(applicationContext, Config::class.java)
        //    startActivity(intent)
        //}

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


    private enum class ActivityType{
        HOME, MAP, ALARMS
    }
}

