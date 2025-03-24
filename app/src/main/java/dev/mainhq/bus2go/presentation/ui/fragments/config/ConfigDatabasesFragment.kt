package dev.mainhq.bus2go.presentation.ui.fragments.config

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.AppThemeState
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.presentation.viewmodels.ConfigurationStateViewModel
import dev.mainhq.bus2go.workers.DatabaseDownloadManagerWorker

class ConfigDatabasesFragment: Fragment(R.layout.fragment_config_database) {

	//store states here (or perhaps inside a ViewModel)
	private var isStmChecked = true
	private var isExoChecked = false

	private val viewModel : ConfigurationStateViewModel by activityViewModels()

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
						WorkManager.getInstance(requireContext())
							.beginWith(OneTimeWorkRequest.Builder(DatabaseDownloadManagerWorker::class.java)
								.addTag("DbDownloadTask")
								.setInputData(data)
								.build())
							//.then(OneTimeWorkRequest.Builder(DatabaseInstallManagerWorker::class.java)
							//	.addTag("DbInitTask")
							//	.build())
							.enqueue()
						dialogInterface.dismiss()
						AppThemeState.turnOffDbUpdateChecking()
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
						viewModel.triggerEvent(true)
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