package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dev.mainhq.bus2go.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//TODO eventually there will either be a special ip address, or a list of available domain names...

class ConfigServerFragment: Fragment(R.layout.fragment_config_server) {

	private val sharedViewModel: ConfigSharedViewModel by activityViewModels()
	private val viewModel: ConfigServerFragmentViewModel by activityViewModels()

	//stores the bus2go server to use as a realtime data server and update and shit...
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val button = view.findViewById<MaterialButton>(R.id.config_select_server_continue_button)
		button.setOnClickListener{
			if (viewModel.buttonText.value == "Skip") {
				MaterialAlertDialogBuilder(requireContext())
					.setTitle("Skip?")
					.setMessage("Are you sure you want to skip? (You may configure this later).")
					.setPositiveButton("Yes"){ dialogInterface, _ ->
						sharedViewModel.triggerEvent(true)
						dialogInterface.dismiss()
					}
					.setNegativeButton("Cancel"){ dialogInterface, _ ->
						dialogInterface.dismiss()
					}
					.show()
			}
			else {
				//TODO try the ip/domain name to see if it is a valid bus2go backend and if it is on...
				sharedViewModel.setFragment(FragmentUsed.DATABASES)
			}
		}

		val textInput = view.findViewById<TextInputEditText>(R.id.config_select_server_textInputEditText)
		textInput.setText(viewModel.textInputText.value)

		viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
				viewModel.textInputText.collect{
					textInput.setOnFocusChangeListener { _, hasFocus ->
						if (!hasFocus) textInput.setText(it)
					}
				}
			}
		}

		textInput.addTextChangedListener(object: TextWatcher{
				override fun afterTextChanged(query: Editable?) {
					viewModel.setServer(query?.toString()?.replace("\n", "") ?: "")
				}

				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
		})

		viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
				//TODO on error, the small top text in the textLayout should become red and say invalid
				// something
				viewModel.buttonText.collect{
					button.text = it
				}
			}
		}

		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner,
			object: OnBackPressedCallback(true){
				override fun handleOnBackPressed() {
					sharedViewModel.setFragment(FragmentUsed.THEME)
				}
		})
	}
}