package app.bus2go.android.presentation.config

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.materialswitch.MaterialSwitch
import app.bus2go.android.Bus2GoApplication
import app.bus2go.android.R
import app.bus2go.android.utils.launchViewModelCollectLatest

class ConfigThemeFragment: Fragment(R.layout.fragment_config_theme) {

	private val prevFrag = FragmentUsed.WELCOME
	private val nextFrag = FragmentUsed.SERVER

	private val sharedViewModel: ConfigSharedViewModel by activityViewModels()

	private val viewModel: ConfigThemeFragmentViewModel by viewModels {
		object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				@Suppress("UNCHECKED_CAST")
				return ConfigThemeFragmentViewModel(
					(requireActivity().application as Bus2GoApplication).commonModule.settingsRepository,
				) as T
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val switch = view.findViewById<MaterialSwitch>(R.id.configSelectThemeSwitch)

		switch.setOnClickListener {
			//TODO should save that state right away
			viewModel.toggleDarkMode()
		}
		launchViewModelCollectLatest(viewModel.isDarkMode){
			switch.isChecked = it
			if (it) switch.text = "Dark"
			else switch.text = "Light"
		}

		sharedViewModel.setButtonState(ConfigButtonState.Next{
			sharedViewModel.setFragment(nextFrag)
		})

		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(prevFrag)
				isEnabled = false
			}
		})
	}
}