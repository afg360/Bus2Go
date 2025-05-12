package dev.mainhq.bus2go.presentation.settings

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.worker.UpdateManagerWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SettingsMainFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener
{

    private val sharedViewModel: SettingsSharedViewModel by activityViewModels()
    private val viewModel: SettingsMainFragmentViewModel by viewModels{
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return (requireActivity().application as Bus2GoApplication).let {
                    SettingsMainFragmentViewModel(
                        it.appModule.checkIsBus2GoServer,
                        it.commonModule.saveBus2GoServer
                    ) as T
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_main, rootKey)

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
                sharedViewModel.setLoading(true)
                viewModel.checkIsBus2GoServer(newValue as String)
                false
            }
        }

        preferenceManager.findPreference<Preference>("update-entry")?.also{
            it.setOnPreferenceClickListener {
                sharedViewModel.setFragment(FragmentUsed.UPDATES)
                true
            }
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
                //FIXME wrong place to be...
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.toastText.collect{
                    sharedViewModel.setLoading(false)
                    Toast.makeText(requireContext(), it.string, Toast.LENGTH_SHORT).show()
                    val editTextPreference = preferenceManager.findPreference<EditTextPreference>("server-choice")
                    if (it.isValid){
                        editTextPreference?.text = it.data
                    }
                    else editTextPreference?.text = ""
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
