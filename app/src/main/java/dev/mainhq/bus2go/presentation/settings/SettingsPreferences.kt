package dev.mainhq.bus2go.presentation.settings

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.worker.UpdateManagerWorker
import java.util.concurrent.TimeUnit

class SettingsPreferences : PreferenceFragmentCompat() ,
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        //TODO perform a migration from sharedPreferences to DataStorePreferences if needed, by checking
        // some field existence...

        //temporarily output the current software version
        findPreference<Preference?>("info")?.let {
            val packageInfo: PackageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName ?: throw IllegalStateException("Cannot have a null value version name!")
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
                    val isOn = sharedPreferences.getBoolean("update-notifications", false)
                    if (isOn) {
                        Log.d("UPDATES", "Notifications enabled")
                        context?.also{ context ->
                            WorkManager.getInstance(context).enqueue(
                                PeriodicWorkRequestBuilder<UpdateManagerWorker>(1, TimeUnit.DAYS)
                                    .setConstraints(
                                        Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.UNMETERED)
                                            .build()
                                    )
                                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                                    .addTag(UpdateManagerWorker.TAG)
                                    .build()
                            )
                        }
                    }
                    else {
                        Log.d("UPDATES", "Disabled notifications")
                        context?.also { context ->
                            WorkManager.getInstance(context)
                                .cancelAllWorkByTag(UpdateManagerWorker.TAG)
                        }
                    }
                }
                "dark-mode" -> {
                    Log.d("Preferences", "Switched dark-mode state")
                    (host as SettingsActivity).changeTheme()
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
