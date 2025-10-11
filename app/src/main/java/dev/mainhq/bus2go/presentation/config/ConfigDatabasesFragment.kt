package dev.mainhq.bus2go.presentation.config

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.presentation.core.state.AppThemeState
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.databinding.FragmentConfigDatabaseBinding
import dev.mainhq.bus2go.databinding.FragmentConfigWelcomeBinding
import dev.mainhq.bus2go.utils.launchViewModelCollect
import kotlinx.coroutines.Dispatchers
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

	private var _binding: FragmentConfigDatabaseBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentConfigDatabaseBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)


		binding.configStmDatabaseCheckBox.also {
			it.isChecked = viewModel.isStmChecked()
			it.setOnCheckedChangeListener { _, _ -> viewModel.toggleStm() }
		}

		binding.configExoDatabaseCheckBox.also {
			it.isChecked = viewModel.isExoChecked()
			it.setOnCheckedChangeListener { _, _ -> viewModel.toggleExo() }
		}


		launchViewModelCollect(viewModel.dbToDownload){
			binding.configDownloadDatabaseContinueButton.text = if (it == null) "Skip" else "Continue"
		}
		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
			override fun handleOnBackPressed() {
				sharedViewModel.setFragment(prevFrag)
				isEnabled = false
			}
		})

		binding.configDownloadDatabaseContinueButton.setOnClickListener {
			//if none checked, display the skip, and create a dialog for a skip
			if (binding.configDownloadDatabaseContinueButton.text == "Continue") {
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
							viewModel.scheduleDownloadWork()
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

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}