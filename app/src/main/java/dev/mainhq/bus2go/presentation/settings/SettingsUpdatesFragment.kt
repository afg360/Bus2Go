package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.databinding.FragmentSettingsUpdatesBinding
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest

class SettingsUpdatesFragment: Fragment() {

	private val sharedViewModel: SettingsSharedViewModel by activityViewModels()

	private val viewModel: SettingsUpdatesFragmentViewModel by viewModels {
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				@Suppress("UNCHECKED_CAST")
				return (this@SettingsUpdatesFragment.requireActivity().application as Bus2GoApplication)
					.let{
						SettingsUpdatesFragmentViewModel(
							it.commonModule.settingsRepository,
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

	//TODO actually use the logic of the 4 switches, for now not the prio to make it work

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		requireActivity().onBackPressedDispatcher.addCallback(
			this,
			object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(FragmentUsed.MAIN)
				isEnabled = false
			}
		})

		/* App Updates */
		binding.settingsAppUpdatesView.setOnClickListener {
			viewModel.toggleIsAppUpdatesNotifOn()
		}
		binding.settingsAppUpdatesToggleSwitchView.setOnClickListener {
			binding.settingsAppUpdatesView.performClick()
		}

		launchViewModelCollectLatest(viewModel.isAppUpdatesNotifOn) {
			binding.settingsAppUpdatesToggleSwitchView.isChecked = it
			binding.settingsAppAutoUpdatesView.isEnabled = it
			binding.settingsAppAutoUpdatesView.children.forEach { child ->
				child.isEnabled = it
			}
		}

		/* App Auto Updates */
		binding.settingsAppAutoUpdatesView.setOnClickListener {
			viewModel.toggleIsAutoAppUpdatesOn()
		}
		binding.settingsAppAutoUpdatesToggleSwitchView.setOnClickListener {
			binding.settingsAppAutoUpdatesView.performClick()
		}

		launchViewModelCollectLatest(viewModel.isAutoAppUpdatesOn) {
			binding.settingsAppAutoUpdatesToggleSwitchView.isChecked = it
		}

		/* Manual Database Downloads/Updates */
		binding.settingsDatabaseManualDownloadView.setOnClickListener {
			sharedViewModel.setFragment(FragmentUsed.DATABASE_DOWNLOADS)
		}

		/* Database Updates */
		binding.settingsDatabaseUpdatesView.setOnClickListener {
			viewModel.toggleIsDbUpdatesNotifOn()
		}
		binding.settingsDatabaseUpdatesToggleSwitchView.setOnClickListener {
			binding.settingsDatabaseUpdatesView.performClick()
		}
		launchViewModelCollectLatest(viewModel.isDbUpdatesNotifOn) {
			binding.settingsDatabaseUpdatesToggleSwitchView.isChecked = it
			binding.settingsAutoDatabaseUpdatesView.isEnabled = it
			binding.settingsAutoDatabaseUpdatesView.children.forEach { child ->
				child.isEnabled = it
			}
		}

		/* Automatic Database Updates */
		binding.settingsAutoDatabaseUpdatesView.setOnClickListener {
			viewModel.toggleIsDbAutoUpdatesOn()
		}
		binding.settingsAutoDatabaseUpdatesToggleSwitchView.setOnClickListener {
			binding.settingsAutoDatabaseUpdatesView.performClick()
		}

		launchViewModelCollectLatest(viewModel.isDbAutoUpdatesOn) {
			binding.settingsAutoDatabaseUpdatesToggleSwitchView.isChecked = it
		}
	}
}