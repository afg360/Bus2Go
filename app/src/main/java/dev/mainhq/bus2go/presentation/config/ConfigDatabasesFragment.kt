package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.presentation.core.state.AppThemeState
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.worker.DatabaseDownloadManagerWorker
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.di.DebugAppContainer
import dev.mainhq.bus2go.domain.entity.DbToDownload

class ConfigDatabasesFragment: Fragment(R.layout.fragment_config_database) {

	//store states here (or perhaps inside a ViewModel)
	private var isStmChecked = true
	private var isExoChecked = false

	private val sharedViewModel : ConfigSharedViewModel by activityViewModels()
	private val viewModel: ConfigDatabasesFragmentViewModel by viewModels {
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return ConfigDatabasesFragmentViewModel(
					//TODO this is shit
					((this@ConfigDatabasesFragment.requireActivity().application as Bus2GoApplication)
						.appContainer as DebugAppContainer).scheduleDownloadDatabaseTask
				) as T
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val continueButton =  view.findViewById<MaterialButton>(R.id.configDownloadDatabaseContinueButton)

		view.findViewById<MaterialCheckBox>(R.id.configStmDatabaseCheckBox).setOnCheckedChangeListener { compoundButton, b ->
			isStmChecked = b
			setButtonText(continueButton)
		}

		view.findViewById<MaterialCheckBox>(R.id.configExoDatabaseCheckBox).setOnCheckedChangeListener { compoundButton, b ->
			isExoChecked = b
			setButtonText(continueButton)
		}

		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(FragmentUsed.THEME)
			}
		})

		continueButton.setOnClickListener {
			//if none checked, display the skip, and create a dialog for a skip
			if (continueButton.text == "Continue") {
				val data = workDataOf(Pair("stm", isStmChecked), Pair("exo", isExoChecked))
				//TODO let the user know how much data it may take...
				MaterialAlertDialogBuilder(requireContext())
					.setTitle("Confirm")
					//list packages
					.setMessage("Are you sure to download the selected packages for download?")
					.setPositiveButton("Yes"){ dialogInterface, _ ->
						Log.d("DATABASE-CONFIG", "Initiating download of data")
						viewModel.download(DbToDownload.STM)
						dialogInterface.dismiss()
						AppThemeState.turnOffDbUpdateChecking()
						sharedViewModel.triggerEvent(true)
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
						sharedViewModel.triggerEvent(true)
					}
					.setNegativeButton("Cancel"){ dialogInterface, _ ->
						dialogInterface.dismiss()
					}
					.show()
			}
		}
	}

	private fun setButtonText(button: MaterialButton){
		if (!isStmChecked && !isExoChecked){
			button.text = "Skip"
		}
		else{
			button.text = "Continue"
		}
	}
}