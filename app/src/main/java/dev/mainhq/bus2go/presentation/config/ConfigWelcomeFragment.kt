package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.databinding.FragmentConfigWelcomeBinding

class ConfigWelcomeFragment: Fragment(R.layout.fragment_config_welcome) {

	private val nextFrag = FragmentUsed.THEME
	private val sharedViewModel: ConfigSharedViewModel by activityViewModels()
	private var _binding: FragmentConfigWelcomeBinding? = null
	private val binding
		get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentConfigWelcomeBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		sharedViewModel.setButtonState(ConfigButtonState.Next{
			sharedViewModel.setFragment(FragmentUsed.THEME)
		})
	}
}