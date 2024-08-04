package dev.mainhq.bus2go.fragments.preferences

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Settings

class SettingsPreferences : PreferenceFragmentCompat() ,
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        
        //temporarily output the current software version
         findPreference<Preference?>("info")?.let {
            val packageInfo: PackageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            it.summary = "Software version: $versionName"
        }
        
        
        preferenceManager.findPreference<EditTextPreference>("server-choice")?.also{
            it.setOnPreferenceChangeListener { _, newValue ->
                val newValueString = newValue as String
                it.text = newValueString
                //could be a domain name or an ip address so don't use numpad
                
                Toast.makeText(requireContext(), "Server changed", Toast.LENGTH_SHORT).show()
                true
            }
        }
        
        //todo: fragment with more info and help
        //how to contribute, etc
        //version of the database
    }
    
    /*
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == "server-choice"){
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.preferencesFragmentContainer, ServerPreferences())
                .addToBackStack(null)
                .commit()
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }
     */
    
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
