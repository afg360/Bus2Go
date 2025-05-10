package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//TODO eventually there will either be a special ip address, or a list of available domain names...

class ConfigServerFragment: Fragment(R.layout.fragment_config_server) {

	private val sharedViewModel: ConfigSharedViewModel by activityViewModels()
	private val viewModel: ConfigServerFragmentViewModel by activityViewModels{
		object: ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return ConfigServerFragmentViewModel(
					(requireActivity().application as Bus2GoApplication).appModule.checkIsBus2GoServer
				) as T
			}
		}
	}

	//stores the bus2go server to use as a realtime data server and update and shit...
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val progressIndicator = view.findViewById<CircularProgressIndicator>(R.id.config_server_continue_button_progress_indicator)

		val textInput = view.findViewById<TextInputEditText>(R.id.config_select_server_textInputEditText)
		textInput.setText(viewModel.textInputText.value)

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
			else viewModel.checkIsBus2GoServer()
		}

		viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
				//TODO try the ip/domain name to see if it is a valid bus2go backend and if it is on...
				viewModel.serverResponse.collect{
					when(it){
						is UiState.Error -> {
							//if device not connected to internet, make a Toast to notify user
							progressIndicator.visibility = View.GONE
							button.text = viewModel.buttonText.value
							button.isEnabled = true
							Toast.makeText(requireContext(), "Not connected to the internet...?", Toast.LENGTH_SHORT)
								.show()
						}
						UiState.Loading -> {
							button.isEnabled = false
							button.text = ""
							progressIndicator.visibility = View.VISIBLE
						}
						is UiState.Success<Boolean> -> {
							progressIndicator.visibility = View.GONE
							button.text = viewModel.buttonText.value
							button.isEnabled = true
							//if the response was valid, go to databases
							if (it.data) sharedViewModel.setFragment(FragmentUsed.DATABASES)
							//else show an error on the inputText, and display "Skip" on the button (or perhaps a retry)
							else textInput.error = "Invalid Bus2Go server"
						}
						UiState.Init -> {
							progressIndicator.visibility = View.GONE
							button.text = viewModel.buttonText.value
							button.isEnabled = true
						}
					}
				}
			}
		}

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
					viewModel.setServer(query?.toString() ?: "")
				}

				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
		})

		viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
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