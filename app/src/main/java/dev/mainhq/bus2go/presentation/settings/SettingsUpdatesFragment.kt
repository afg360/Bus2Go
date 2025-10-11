package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R

class SettingsUpdatesFragment: PreferenceFragmentCompat() {

	private val sharedViewModel: SettingsSharedViewModel by activityViewModels()

	private val viewModel: SettingsUpdateFragmentViewModel by viewModels {
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return (this@SettingsUpdatesFragment.requireActivity().application as Bus2GoApplication)
					.let{
						SettingsUpdateFragmentViewModel(
							it.appModule.scheduleDownloadDatabaseTask,
						) as T
					}
			}
		}
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.settings_updates, rootKey)

		requireActivity().onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(FragmentUsed.MAIN)
				isEnabled = false
			}
		})

		//use the same fragment as inside the configs....
		findPreference<Preference>("manual-download")?.setOnPreferenceClickListener {
			val options = arrayOf("Stm", "Exo")
			val checkedItems = booleanArrayOf(false, false)
			MaterialAlertDialogBuilder(requireActivity())
				.setTitle("Select databases")
				.setMultiChoiceItems(options, checkedItems){ _, which, isChecked ->
					checkedItems[which] = isChecked
				}
				.setPositiveButton("Positive"){ dialogInterface, foo ->
					if (checkedItems[0]) viewModel.downloadStm()
					if (checkedItems[1]) viewModel.downloadExo()
					dialogInterface.dismiss()
				}
				.setNegativeButton("Cancel"){ dialogInterface, foo ->
					dialogInterface.cancel()
				}
				.show()
			true
		}
	}
}