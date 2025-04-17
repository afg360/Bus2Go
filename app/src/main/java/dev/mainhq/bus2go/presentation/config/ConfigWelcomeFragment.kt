package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import dev.mainhq.bus2go.R

class ConfigWelcomeFragment: Fragment(R.layout.fragment_config_welcome) {

	private val viewModel: ConfigSharedViewModel by activityViewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		view.findViewById<MaterialButton>(R.id.configureContinueButton).setOnClickListener {
			viewModel.setFragment(FragmentUsed.THEME)
		}
	}
}