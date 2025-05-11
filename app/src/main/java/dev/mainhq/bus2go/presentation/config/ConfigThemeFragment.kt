package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import dev.mainhq.bus2go.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ConfigThemeFragment: Fragment(R.layout.fragment_config_theme) {

	private val sharedViewModel: ConfigSharedViewModel by activityViewModels()

	private val viewModel: ConfigThemeFragmentViewModel by activityViewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val switch = view.findViewById<MaterialSwitch>(R.id.configSelectThemeSwitch)
		switch.setOnCheckedChangeListener { _, boolean ->
			//TODO should save that state right away
			viewModel.setDarkMode(boolean)
		}

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
				viewModel.darkMode.collect{
					switch.isChecked = it
					if (it) switch.text = "Dark"
					else switch.text = "Light"
				}
			}
		}

		view.findViewById<MaterialButton>(R.id.configSelectThemeContinueButton).setOnClickListener {
			sharedViewModel.setFragment(FragmentUsed.SERVER)
		}

		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(FragmentUsed.WELCOME)
			}
		})
	}
}