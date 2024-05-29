package dev.mainhq.bus2go

import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationBarView
import dev.mainhq.bus2go.fragments.Alarms
import dev.mainhq.bus2go.fragments.Home
import dev.mainhq.bus2go.fragments.Map
import dev.mainhq.bus2go.viewmodel.AlarmCreationViewModel

//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)

class MainActivity() : BaseActivity() {

    private lateinit var activityType : ActivityType
    //var realTime : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //if (false) { //TODO check if config file exists
        //    val intent = Intent(applicationContext, Config::class.java)
        //    startActivity(intent)
        //}

        /* THEME must be set before setContentView */
        //TODO must also setup the color of all the drawables needed
        //AppThemeState.setTheme(this)
        setContentView(R.layout.main_activity)
        //val settingsData = SettingsData(applicationContext)//.setTheme(applicationContext)
        //realTime = settingsData.isRealTime()

        activityType = ActivityType.HOME
        val home = Home()
        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, home).commit()
        //setBackground()
        //setButtons()

        val alarmViewModel = ViewModelProvider(this)[AlarmCreationViewModel::class.java]
        val bottomNav = findViewById<NavigationBarView>(R.id.bottomNavBarView)
        bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.homeScreenButton -> {
                    // Respond to navigation item 1 click
                    if (activityType != ActivityType.HOME) {
                        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, Home()).commit()
                        activityType = ActivityType.HOME
                    }
                    true
                }
                R.id.mapButton -> {
                    // Respond to navigation item 2 click
                    if (activityType != ActivityType.MAP) {
                        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, Map()).commit()
                        activityType = ActivityType.MAP
                    }
                    true
                }
                R.id.alarmsButton -> {
                    // Respond to navigation item 2 click
                    if (activityType != ActivityType.ALARMS) {
                        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, Alarms(alarmViewModel)).commit()
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

    private enum class ActivityType{
        HOME, MAP, ALARMS
    }
}

