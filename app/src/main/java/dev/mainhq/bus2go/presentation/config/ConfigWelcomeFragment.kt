package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.databinding.FragmentConfigWelcomeBinding

class ConfigWelcomeFragment: Fragment(R.layout.fragment_config_welcome) {

	private val nextFrag = FragmentUsed.THEME
	private val viewModel: ConfigSharedViewModel by activityViewModels()
	//private lateinit var binding: FragmentConfigWelcomeBinding

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		//binding = FragmentConfigWelcomeBinding.inflate(layoutInflater)

		view.findViewById<MaterialButton>(R.id.config_continue_button).setOnClickListener {
			viewModel.setFragment(nextFrag)
		}
	}
}