package dev.mainhq.bus2go.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Settings

class SettingsPreferences : PreferenceFragmentCompat() ,
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.also{
            when (key){
                "real-time-data" -> {
                    Log.d("REALTIME", "feedback clicked")
                }
                "update-notifications" -> {
                    Log.d("UPDATES", "Notifications enabled")
                }
                "dark-mode" -> {
                    Log.d("Preferences", "Dark-mode enabled")
                    (host as Settings).changeTheme()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }
}
