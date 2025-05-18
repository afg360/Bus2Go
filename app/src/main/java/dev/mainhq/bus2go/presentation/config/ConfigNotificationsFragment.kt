package dev.mainhq.bus2go.presentation.config

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//TODO display in the fragment: You are almost all set!

class ConfigNotificationsFragment: Fragment(R.layout.fragment_config_notifications) {

	//private val prevFrag = FragmentUsed.DATABASES

	private val sharedViewModel: ConfigSharedViewModel by activityViewModels()
	private val viewModel: ConfigNotificationsFragmentViewModel by activityViewModels{
		object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
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


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		view.findViewById<MaterialButton>(R.id.config_notification_confirm_button).setOnClickListener {
			if (viewModel.anyNotifSet()){
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					if (ContextCompat.checkSelfPermission(
							requireContext(),
							Manifest.permission.POST_NOTIFICATIONS
						) == PackageManager.PERMISSION_GRANTED
					) viewModel.saveSettings()
					else requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
				}
				else {
					if (!viewModel.saveSettings()){
						Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
							.show()
					}
				}
			}
			sharedViewModel.triggerEvent(true)
		}

		val appNotifs = view.findViewById<MaterialSwitch>(R.id.config_notification_switch_app_updates)
		val dbNotifs = view.findViewById<MaterialSwitch>(R.id.config_notification_switch_db_updates)

		appNotifs.setOnCheckedChangeListener { _, boolean ->
			viewModel.setAppUpdateNotifs(boolean)
		}

		dbNotifs.setOnCheckedChangeListener { _, boolean ->
			viewModel.setDbUpdateNotifs(boolean)
		}

		viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main){
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
				viewModel.appUpdateNotifs.collect{
					appNotifs.isChecked = it
				}
			}
		}

		viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main){
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
				viewModel.dbUpdateNotifs.collect{
					dbNotifs.isChecked = it
				}
			}
		}

		/*
		requireActivity().onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(prevFrag)
				isEnabled = false
			}
		})
		 */
	}

}