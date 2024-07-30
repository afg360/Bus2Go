package dev.mainhq.bus2go.fragments

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Settings

class SettingsPreferences : PreferenceFragmentCompat() ,
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        
        //temporarily output the current software version
        val versionPreference: Preference? = findPreference("info")
        versionPreference?.let {
            val packageInfo: PackageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            it.summary = "Software version: $versionName"
        }
        //todo: fragment with more info and help
        //how to contribute, etc
        //version of the database
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
