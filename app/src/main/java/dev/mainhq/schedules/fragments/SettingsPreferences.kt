package dev.mainhq.schedules.fragments

import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import dev.mainhq.schedules.R

class SettingsPreferences : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        findPreference<SwitchPreferenceCompat>("notifications")
            ?.setOnPreferenceChangeListener { _, newValue ->
                Log.d("Preferences", "Notifications enabled: $newValue")
                true // Return true if the event is handled.
            }

        findPreference<Preference>("feedback")
            ?.setOnPreferenceClickListener {
                Log.d("Preferences", "Feedback was clicked")
                true // Return true if the click is handled.
            }

    }
}
