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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.databinding.FragmentConfigDatabaseBinding
import dev.mainhq.bus2go.domain.entity.DatabaseState
import dev.mainhq.bus2go.presentation.settings.SettingsDownloadDatabasesFragment
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest
import dev.mainhq.bus2go.utils.makeVisible
import dev.mainhq.bus2go.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfigDatabasesFragment: Fragment(R.layout.fragment_config_database) {

	private val sharedViewModel : ConfigSharedViewModel by activityViewModels()

	private val prevFrag = FragmentUsed.SERVER
	private val nextFrag = FragmentUsed.NOTIFICATIONS

	private val viewModel: ConfigDatabasesFragmentViewModel by activityViewModels{
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				@Suppress("UNCHECKED_CAST")
				return (this@ConfigDatabasesFragment.requireActivity().application as Bus2GoApplication).let {
					ConfigDatabasesFragmentViewModel(
						it.commonModule.observeDownloadDatabaseTask,
						it.commonModule.scheduleDownloadDatabaseTask
					) as T
				}
			}
		}
	}

	private var _binding: FragmentConfigDatabaseBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentConfigDatabaseBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val linearLayoutManager = LinearLayoutManager(view.context)
		linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
		binding.configDownloadDatabaseRecyclerView.layoutManager = linearLayoutManager
		val adapter = SettingsDownloadDatabasesFragment.MyAdapter(
			viewModel.databases.value,
			downloadedOnClickListener = { },
			onDownloadOnClickListener = {
				requireContext().toast("Downloading database $it...")
			},
			onDownloadCompletedOnClickListener = {
				requireContext().toast("Please restart the app")
			},
			deleteDownloadedOnLongClickListener = { false },
			notDownloadedOnClickListener = { dbState, linearProgressBar ->
				dbState as DatabaseState
				MaterialAlertDialogBuilder(requireContext())
					.setTitle("Download ${dbState.db}?")
					.setMessage("Are you sure to download the database for ${dbState.db}?")
					.setPositiveButton("Download") { dialogInterface, _ ->
						viewModel.downloadDatabase(dbState.db)
						linearProgressBar.makeVisible()
					}
					.setNegativeButton("Cancel") { dialogInterface, _ ->
						dialogInterface.dismiss()
					}
					.setOnCancelListener { dialogInterface ->
						dialogInterface.dismiss()
					}
					.show()
			},
		)

		binding.configDownloadDatabaseRecyclerView.adapter = adapter

		launchViewModelCollectLatest(viewModel.databases) {
			adapter.setList(it)
		}

		sharedViewModel.setButtonState(ConfigButtonState.Next{
			sharedViewModel.setFragment(nextFrag)
		})

		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner,
			object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					sharedViewModel.setFragment(prevFrag)
					isEnabled = false
				}
			})
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}