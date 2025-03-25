package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import dev.mainhq.bus2go.R

class ConfigWelcomeFragment: Fragment(R.layout.fragment_config_welcome) {

	private val viewModel: ConfigurationStateViewModel by activityViewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.setFragmentTag(this::class.java.name)

		view.findViewById<MaterialButton>(R.id.configureContinueButton).setOnClickListener {
			val transaction = parentFragmentManager.beginTransaction()
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.configActivityFragmentContainer, ConfigThemeFragment())
				.commit()
		}
	}
}