package dev.mainhq.bus2go

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import dev.mainhq.bus2go.fragments.SettingsPreferences

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("dark-mode", true)) setTheme(R.style.Theme_Schedules_Dark)
        else setTheme(R.style.Theme_Schedules)
        setContentView(R.layout.settings)

        val menuBar = findViewById<MaterialToolbar>(R.id.settingsToolBar)
        menuInflater.inflate(R.menu.app_bar_settings, menuBar.menu)
        menuBar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.settingsBackButton -> {
                    finish()
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.preferencesFragmentContainer, SettingsPreferences())
            .commit()

    }

    fun changeTheme(){
        finish()
        startActivity(intent)
    }

}
