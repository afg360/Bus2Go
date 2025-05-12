package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.presentation.core.state.AppThemeState
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConfigDatabasesFragment: Fragment(R.layout.fragment_config_database) {

	private val sharedViewModel : ConfigSharedViewModel by activityViewModels()

	private val prevFrag = FragmentUsed.SERVER
	private val nextFrag = FragmentUsed.NOTIFICATIONS

	private val viewModel: ConfigDatabasesFragmentViewModel by activityViewModels{
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return ConfigDatabasesFragmentViewModel(
					//TODO this is shit
					(this@ConfigDatabasesFragment.requireActivity().application as Bus2GoApplication)
						.appModule.scheduleDownloadDatabaseTask
				) as T
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val continueButton =  view.findViewById<MaterialButton>(R.id.configDownloadDatabaseContinueButton)
		val stmCheckbox = view.findViewById<MaterialCheckBox>(R.id.configStmDatabaseCheckBox)
		val exoCheckBox = view.findViewById<MaterialCheckBox>(R.id.configExoDatabaseCheckBox)

		stmCheckbox.isChecked = viewModel.isStmChecked()
		exoCheckBox.isChecked = viewModel.isExoChecked()

		stmCheckbox.setOnCheckedChangeListener { _, _ -> viewModel.toggleStm() }
		exoCheckBox.setOnCheckedChangeListener { _, _ -> viewModel.toggleExo() }

		viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
				viewModel.dbToDownload.collect{
					continueButton.text = if (it == null) "Skip" else "Continue"
				}
			}
		}

		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(prevFrag)
			}
		})

		continueButton.setOnClickListener {
			//if none checked, display the skip, and create a dialog for a skip
			if (continueButton.text == "Continue") {
				//TODO let the user know how much data it may take...
				MaterialAlertDialogBuilder(requireContext())
					.setTitle("Confirm")
					//list packages
					.setMessage("Are you sure to download the selected packages for download?")
					.setPositiveButton("Yes"){ dialogInterface, _ ->
						Log.d("DATABASE-CONFIG", "Initiating download of data")
						viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
							dialogInterface.dismiss()
							AppThemeState.turnOffDbUpdateChecking()
							sharedViewModel.setFragment(nextFrag)
						}
					}
					.setNegativeButton("Cancel"){ dialogInterface, _ -> /*close the dialog*/ dialogInterface.dismiss() }
					.show()
			}
			else{
				MaterialAlertDialogBuilder(requireContext())
					.setTitle("Are you sure?")
					.setMessage("You won't be able to properly use the app without a database installed (you can always download one later)")
					.setPositiveButton("Yes, skip") { dialogInterface, _ ->
						dialogInterface.dismiss()
						sharedViewModel.setFragment(nextFrag)
					}
					.setNegativeButton("Cancel"){ dialogInterface, _ ->
						dialogInterface.dismiss()
					}
					.show()
			}
		}
	}
}