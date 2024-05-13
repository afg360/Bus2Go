package dev.mainhq.schedules

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.mainhq.schedules.fragments.Favourites
import dev.mainhq.schedules.fragments.Home

//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)

class MainActivity : AppCompatActivity() {

    private lateinit var activityType : ActivityType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //todo
        if (false) { //TODO check if config file exists
            val intent = Intent(this.applicationContext, Config::class.java)
            startActivity(intent)
        }
        setContentView(R.layout.main_activity)
        activityType = ActivityType.HOME
        setFragment()
        setBackground()
        setButtons()
    }

    private fun setFragment(){
        when(activityType){
            ActivityType.HOME -> {
                val home = Home()
                supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, Home()).commit()
            }
            ActivityType.MAP -> {
                supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, Home()).commit()
            }
            ActivityType.ALARMS -> {
                supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, Home()).commit()
            }
        }
    }

    private fun setButtons(){
        findViewById<LinearLayout>(R.id.homeScreenButton).setOnClickListener {
            if (activityType != ActivityType.HOME){
                activityType = ActivityType.HOME
                supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, Home()).commit()
                setBackground()
            }
        }
        findViewById<LinearLayout>(R.id.mapButton).setOnClickListener {
            setBackground()
        }
        findViewById<LinearLayout>(R.id.alarmsButton).setOnClickListener {
            setBackground()
        }
    }

    private fun setBackground(){
        setDefaultBackgroundColors()
        when(activityType){
            ActivityType.HOME -> {
                findViewById<LinearLayout>(R.id.homeScreenButton).setBackgroundColor(com.google.android.material.R.attr.colorControlHighlight)
            }
            ActivityType.MAP -> {
                findViewById<LinearLayout>(R.id.mapButton).setBackgroundColor(com.google.android.material.R.attr.colorControlHighlight)
            }
            ActivityType.ALARMS -> {
                findViewById<LinearLayout>(R.id.alarmsButton).setBackgroundColor(com.google.android.material.R.attr.colorControlHighlight)
            }
        }
    }

    private fun setDefaultBackgroundColors(){
        findViewById<LinearLayout>(R.id.homeScreenButton).setBackgroundColor(Color.TRANSPARENT)
        findViewById<LinearLayout>(R.id.mapButton).setBackgroundColor(Color.TRANSPARENT)
        findViewById<LinearLayout>(R.id.alarmsButton).setBackgroundColor(Color.TRANSPARENT)
    }
}

enum class ActivityType{
    HOME, MAP, ALARMS
}