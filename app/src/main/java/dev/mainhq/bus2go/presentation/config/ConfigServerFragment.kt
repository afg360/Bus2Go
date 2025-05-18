package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.presentation.core.UiState
import dev.mainhq.bus2go.presentation.core.collectFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

//TODO eventually there will either be a special ip address, or a list of available domain names...

class ConfigServerFragment: Fragment(R.layout.fragment_config_server) {

	private val prevFrag = FragmentUsed.THEME
	private val nextFrag = FragmentUsed.DATABASES

	private val sharedViewModel: ConfigSharedViewModel by activityViewModels()

	//perhaps instead use by viewModels with a savedStateHandle...
	private val viewModel: ConfigServerFragmentViewModel by activityViewModels{
		object: ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return (requireActivity().application as Bus2GoApplication).let{
					ConfigServerFragmentViewModel(
						it.appModule.checkIsBus2GoServer,
						it.commonModule.saveBus2GoServer
					) as T
				}
			}
		}
	}

	//stores the bus2go server to use as a realtime data server and update and shit...
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val progressIndicator = view.findViewById<CircularProgressIndicator>(R.id.config_server_continue_button_progress_indicator)

		val textInput = view.findViewById<TextInputEditText>(R.id.config_select_server_textInputEditText)

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

		collectFlow(viewModel.serverResponse){
			when(it){
				is UiState.Error -> {
					//if device not connected to internet, make a SnackBar and allow retry
					//FIXME this is not the way to do it
					progressIndicator.visibility = View.GONE
					button.text = viewModel.buttonText.value
					button.isEnabled = true
					if (it.message == "Not connected to the internet")
					//TODO add a swippable behaviour bcz this is annoying...
						Snackbar.make(requireContext(), view, it.message, Snackbar.LENGTH_INDEFINITE)
							.setAction("Retry"){
								//FIXME Button quirks because constantly updated...
								viewModel.checkIsBus2GoServer()
							}
							.show()

					else {
						textInput.error = "Invalid Server"
						Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
							.show()
					}
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
					if (it.data) {
						viewModel.cancel()
						sharedViewModel.setFragment(nextFrag)
					}
					//else show an error on the inputText, and display "Skip" on the button (or perhaps a retry)
					else textInput.error = "Invalid server"
				}
				UiState.Init -> {
					progressIndicator.visibility = View.GONE
					button.text = viewModel.buttonText.value
					button.isEnabled = true
				}
			}
		}

		collectFlow(viewModel.textInputText){
			when (it) {
				is UiState.Error -> textInput.error = "Invalid Url"
				UiState.Init -> textInput.setText("")
				UiState.Loading -> TODO("Wtf")
				is UiState.Success<String> -> if (!textInput.hasFocus()) textInput.setText(it.data)
			}
		}

		textInput.addTextChangedListener(object: TextWatcher{
				override fun afterTextChanged(query: Editable?) {
					viewModel.setServer(query?.toString() ?: "")
				}

				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
		})

		collectFlow(viewModel.buttonText){ button.text = it }

		val mainBackPressCallBack = object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				viewModel.cancel()
				sharedViewModel.setFragment(prevFrag)
				//must be reenabled when clicking on the EditTextView
				//isEnabled = false
			}
		}
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, mainBackPressCallBack)

		val backPressedCallback = object : OnBackPressedCallback(false){
			override fun handleOnBackPressed() {
				textInput.clearFocus()
				isEnabled = false
			}
		}
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

		textInput.setOnFocusChangeListener { _, focused ->
			if (focused){
				mainBackPressCallBack.isEnabled = false
				backPressedCallback.isEnabled = true
			}
			else {
				mainBackPressCallBack.isEnabled = true
				backPressedCallback.isEnabled = false
			}
		}
	}
}