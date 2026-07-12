package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.databinding.FragmentSettingsUpdatesBinding

class SettingsUpdatesFragment: Fragment() {

	private val sharedViewModel: SettingsSharedViewModel by activityViewModels()

	private val viewModel: SettingsUpdatesFragmentViewModel by viewModels {
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				@Suppress("UNCHECKED_CAST")
				return (this@SettingsUpdatesFragment.requireActivity().application as Bus2GoApplication)
					.let{
						SettingsUpdatesFragmentViewModel(
							it.commonModule.scheduleDownloadDatabaseTask,
						) as T
					}
			}
		}
	}

	private var _binding: FragmentSettingsUpdatesBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentSettingsUpdatesBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		requireActivity().onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(FragmentUsed.MAIN)
				isEnabled = false
			}
		})

		binding.settingsAppUpdatesView.setOnClickListener {
			//TODO
		}

		binding.settingsAppAutoUpdatesView.setOnClickListener {
			//TODO
		}

		binding.settingsDatabaseManualDownloadView.setOnClickListener {
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

		binding.settingsDatabaseUpdatesView.setOnClickListener {
			//TODO
		}

		binding.settingsAutoDatabaseUpdatesView.setOnClickListener {
			//TODO
		}
	}
}