package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import dev.mainhq.bus2go.R

class ConfigThemeFragment: Fragment(R.layout.fragment_config_theme) {

	private val viewModel: ConfigSharedViewModel by activityViewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		view.findViewById<MaterialSwitch>(R.id.configSelectThemeSwitch)
			.setOnCheckedChangeListener { compoundButton, boolean ->
				if (boolean) {
					compoundButton.text = "Dark"
					//AppThemeState.setTheme(true)
				}
				else {
					compoundButton.text = "Light"
					//AppThemeState.setTheme(false)
				}
		}

		view.findViewById<MaterialButton>(R.id.configSelectThemeContinueButton).setOnClickListener {
			viewModel.setFragment(FragmentUsed.DATABASES)
		}

		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				viewModel.setFragment(FragmentUsed.WELCOME)
			}
		})
	}
}