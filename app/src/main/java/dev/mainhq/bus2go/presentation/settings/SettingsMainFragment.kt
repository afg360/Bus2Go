package dev.mainhq.bus2go.presentation.settings

import android.content.pm.PackageInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
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
        //FIXME May have a race condition bug...
        binding.settingsThemeView.setOnClickListener {
            viewModel.toggleDarkMode()
            toggleTheme()
        }
        binding.settingsThemeToggleSwitchView.setOnClickListener {
            binding.settingsThemeView.performClick()
        }

        launchViewModelCollectLatest(viewModel.isDarkMode) {
            binding.settingsThemeToggleSwitchView.isChecked = it
        }

        /* Data and Syncing Section */
        /* Config Server */
        binding.settingsConfigServerTitle.setOnClickListener {
            //TODO
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Config Server")
                .setView(
                    layoutInflater
                        .inflate(R.layout.fragment_settings_main_config_server_dialog, null)
                        .apply {
                            //Setup basic UI fields from previous settings
                            val serverTypeSwitch = findViewById<MaterialSwitch>(R.id.settings_dialog_select_server_type_switch)
                            serverTypeSwitch.isChecked = !viewModel.serverChoice.value.isSelfHosted
                            val serverTypeSwitchText = findViewById<MaterialTextView>(R.id.settings_dialog_select_server_type_text_view)
                            serverTypeSwitchText.text = if (viewModel.serverChoice.value.isSelfHosted) {
                                "Self-Hosted"
                            }
                            else {
                                "Web"
                            }
                            viewModel.setDialogIsSelfHosted(viewModel.serverChoice.value.isSelfHosted)
                            serverTypeSwitch.setOnClickListener {
                                viewModel.toggleDialogIsSelfHosted()
                                serverTypeSwitchText.text = viewModel.getDialogIsSelfHostedText()
                            }
                            val serverEditText = findViewById<TextInputEditText>(R.id.settings_dialog_select_server_text_input_edit_text)
                            serverEditText.setText(viewModel.serverChoice.value.server)
                            serverEditText.addTextChangedListener(object: TextWatcher {
                                override fun afterTextChanged(editable: Editable?) {
                                    editable?.also {
                                        viewModel.setDialogInput(it.toString())
                                    }
                                }

                                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
                                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
                            })
                        }
                )
                .setPositiveButton("Confirm") { dialogInterface, _ ->
//                    val foo = dialogView.findViewById<MaterialTextView>(R.id.settings_dialog_select_server_type_text_view)
//                    Toast.makeText(it.context, foo.text, Toast.LENGTH_SHORT).show()
                    viewModel.submitDialogFields()
                    dialogInterface.dismiss()
                }
                .setNegativeButton("Delete") { dialogInterface, _ ->
                    //Set the server to be nothing
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Server Entry?")
                        .setMessage("Are you sure you want to delete the server?")
                        .setPositiveButton("Yes") { innerDialogInterface, _ ->
                            viewModel.setDialogIsSelfHosted(true)
                            viewModel.setDialogInput("")
                            viewModel.submitDialogFields()
                            innerDialogInterface.dismiss()
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("No") { innerDialogInterface, _ ->
                            innerDialogInterface.dismiss()
                        }
                        .show()
                }
                .setNeutralButton("Cancel") { dialogInterface, _ ->
                    viewModel.setDialogIsSelfHosted(viewModel.serverChoice.value.isSelfHosted)
                    viewModel.setDialogInput(viewModel.serverChoice.value.server)
                    dialogInterface.dismiss()
                }
                .setOnCancelListener { dialogInterface ->
                    dialogInterface.dismiss()
                }
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
        binding.settingsAboutView.setOnClickListener {  }
    }

    private fun toggleTheme(){
        when(AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        requireActivity().recreate()
    }
}
