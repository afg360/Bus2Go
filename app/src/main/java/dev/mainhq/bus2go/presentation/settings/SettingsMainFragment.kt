package dev.mainhq.bus2go.presentation.settings

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.databinding.FragmentSettingsMainBinding
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest

class SettingsMainFragment : Fragment() {

    private val sharedViewModel: SettingsSharedViewModel by activityViewModels()
    private val viewModel: SettingsMainFragmentViewModel by viewModels{
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return (requireActivity().application as Bus2GoApplication).let {
                    SettingsMainFragmentViewModel(
                        it.commonModule.settingsRepository,
                        it.appModule.checkIsBus2GoServer,
                        it.commonModule.saveBus2GoServer
                    ) as T
                }
            }
        }
    }

    private var _binding: FragmentSettingsMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchViewModelCollectLatest(viewModel.toastText){
            sharedViewModel.setLoading(false)
            Toast.makeText(requireContext(), it.string, Toast.LENGTH_SHORT).show()
            //TODO Dialog part here
//            val editTextPreference = viewModel.settings.value.serverChoice
//                preferenceManager.findPreference<EditTextPreference>("server-choice")
//            if (it.isValid) editTextPreference?.text = it.data
//            else editTextPreference?.text = ""
        }

        launchViewModelCollectLatest(viewModel.isRealTimeOn) {
            binding.settingsRealtimeToggleSwitchView.isChecked = it
        }


        /* General settings Section */
        /* Langs */
        binding.settingsLanguageView.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Language")
                .setSingleChoiceItems(
                    resources.getTextArray(R.array.langs),
                    viewModel.langChoice.value
                ) { _, langPosition ->
                    viewModel.setLang(langPosition)
                }
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .setOnCancelListener { dialogInterface ->
                    dialogInterface.dismiss()
                }
                .show()
        }

        /* Theme */
        launchViewModelCollectLatest(viewModel.isDarkMode) {
            binding.settingsThemeToggleSwitchView.isChecked = it
            (requireActivity() as SettingsActivity).changeTheme(it)
        }
        binding.settingsThemeView.setOnClickListener {
            viewModel.toggleDarkMode()
        }

        /* Data and Syncing Section */
        /* Config Server */
        binding.settingsConfigServerTitle.setOnClickListener {
            //TODO
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Config Server")
                .show()
        }

        /* Updates */
        binding.settingsUpdatesView.setOnClickListener {
            sharedViewModel.setFragment(FragmentUsed.UPDATES)
        }

        /* Real Time Data Updates */
        //TODO
        // launchViewModelCollectLatest()
        binding.settingsRealtimeView.setOnClickListener {
            //TODO toggle the switch
        }

        /* Support Section */
        /* Donate */
        binding.settingsDonateView.setOnClickListener {
            //TODO
        }

        /* Send Feedback */
        binding.settingsFeedbackView.setOnClickListener {
            //TODO
        }


        /* About Section */
        val packageInfo: PackageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionName = packageInfo.versionName ?: throw IllegalStateException("Cannot have a null value version name!")
        binding.settingsAboutDescrTextView.text = "Software version: $versionName"
    }

}
