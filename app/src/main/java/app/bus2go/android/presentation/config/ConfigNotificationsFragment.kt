package app.bus2go.android.presentation.config

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.bus2go.android.Bus2GoApplication
import app.bus2go.android.R
import app.bus2go.android.databinding.FragmentConfigNotificationsBinding
import app.bus2go.android.presentation.main.MainActivity
import app.bus2go.android.utils.launchViewModelCollectLatest

//TODO display in the fragment: You are almost all set!

class ConfigNotificationsFragment: Fragment(R.layout.fragment_config_notifications) {

	private val prevFrag = FragmentUsed.DATABASES

	private val sharedViewModel: ConfigSharedViewModel by activityViewModels()
	private val viewModel: ConfigNotificationsFragmentViewModel by activityViewModels{
		object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				@Suppress("UNCHECKED_CAST")
				return (requireActivity().application as Bus2GoApplication).let{
					ConfigNotificationsFragmentViewModel(
						it.commonModule.saveAllNotifSettings,
					) as T
				}
			}
		}
	}

	private val requestPermissionLauncher = registerForActivityResult(RequestPermission()){ isGranted ->
		if (isGranted){
			println("Granted")
			viewModel.saveSettings()
		}
		else {
			//set everything to false, and don't save (by default all are false)
			println("Not granted...")
		}
	}

	private var _binding: FragmentConfigNotificationsBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentConfigNotificationsBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		launchViewModelCollectLatest(viewModel.appUpdateNotifs){
			binding.configNotificationSwitchAppUpdates.isChecked = it
		}

		launchViewModelCollectLatest(viewModel.dbUpdateNotifs){
			binding.configNotificationSwitchDbUpdates.isChecked = it
		}

		sharedViewModel.setButtonState(ConfigButtonState.Next("Start!") {
			if (viewModel.isAnyNotifSet()){
				//must be sure app has post notification permissions
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					if (ContextCompat.checkSelfPermission(
							requireContext(),
							Manifest.permission.POST_NOTIFICATIONS
						) != PackageManager.PERMISSION_GRANTED
					) {
						requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
					}
				}
			}
			viewModel.saveSettings()
			startActivity(Intent(requireContext(), MainActivity::class.java))
			requireActivity().finish()
		})

		binding.configNotificationSwitchAppUpdates.setOnCheckedChangeListener { _, boolean ->
			viewModel.setAppUpdateNotifs(boolean)
		}

		binding.configNotificationSwitchDbUpdates.setOnCheckedChangeListener { _, boolean ->
			viewModel.setDbUpdateNotifs(boolean)
		}

		requireActivity().onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(prevFrag)
				//isEnabled = false
			}
		})
	}

}