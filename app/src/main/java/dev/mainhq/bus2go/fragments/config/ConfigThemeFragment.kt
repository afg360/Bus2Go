package dev.mainhq.bus2go.fragments.config

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import dev.mainhq.bus2go.AppThemeState
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.viewmodels.ConfigurationStateViewModel

class ConfigThemeFragment: Fragment(R.layout.fragment_config_theme) {

	private val viewModel: ConfigurationStateViewModel by activityViewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.setFragmentTag(this::class.java.name)

		view.findViewById<MaterialSwitch>(R.id.configSelectThemeSwitch).setOnCheckedChangeListener { compoundButton, boolean ->
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
			parentFragmentManager.beginTransaction()
				.replace(R.id.configActivityFragmentContainer, ConfigDatabasesFragment())
				.commit()
		}
	}
}