package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.DatabaseState
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.entity.Progress
import dev.mainhq.bus2go.utils.cleanString
import dev.mainhq.bus2go.utils.isExpired
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest
import dev.mainhq.bus2go.utils.makeGone
import dev.mainhq.bus2go.utils.makeVisible
import dev.mainhq.bus2go.utils.toast

class SettingsDownloadDatabasesFragment(): Fragment() {

	private val sharedViewModel: SettingsSharedViewModel by activityViewModels()

	private val viewModel: SettingsDownloadDatabasesFragmentViewModel by viewModels {
		object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				@Suppress("UNCHECKED_CAST")
				return SettingsDownloadDatabasesFragmentViewModel(
					(requireActivity().application as Bus2GoApplication).commonModule.observeDownloadDatabaseTask,
					(requireActivity().application as Bus2GoApplication).commonModule.scheduleDownloadDatabaseTask,
					(requireActivity().application as Bus2GoApplication).commonModule.deleteDatabase
				) as T
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return RecyclerView(requireContext())
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		requireActivity().onBackPressedDispatcher.addCallback(
			this,
			object: OnBackPressedCallback(true){
				override fun handleOnBackPressed() {
					sharedViewModel.setFragment(FragmentUsed.UPDATES)
					isEnabled = false
				}
			})

		val linearLayoutManager = LinearLayoutManager(view.context)
		linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
		val adapter = MyAdapter(
			viewModel.databases.value,
			downloadedOnClickListener = {
				if (it.isExpired()) {
					MaterialAlertDialogBuilder(requireContext())
						.setTitle("Update ${it.db}?")
						.setMessage("Are you sure to update the database for ${it.db}?")
						.setPositiveButton("Update") { dialogInterface, _ ->
							viewModel.downloadDatabase(it.db)
						}
						.setNegativeButton("Cancel") { dialogInterface, _ ->
							dialogInterface.dismiss()
						}
						.setOnCancelListener { dialogInterface ->
							dialogInterface.dismiss()
						}
						.show()
				}
				else {
					//TODO perhaps make a query to verify if we have the correct version, but
					// code should be already good
					Toast.makeText(requireContext(), "Database is up to date", Toast.LENGTH_SHORT).show()
				}
			},
			onDownloadOnClickListener = {
				requireContext().toast("Downloading database ${it}...")
			},
			onDownloadCompletedOnClickListener = {
				requireContext().toast("Please restart the app")
			},
			deleteDownloadedOnLongClickListener = {
				MaterialAlertDialogBuilder(requireContext())
					.setTitle("Delete ${it.db}?")
					.setMessage("Do you really want to download the ${it.db} database? This action cannot be undone.")
					.setPositiveButton("Delete") { dialogInterface, _ ->
						viewModel.deleteDatabase(it.db)
					}
					.setNegativeButton("Cancel") { dialogInterface, _ ->
						dialogInterface.dismiss()
					}
					.setOnCancelListener { dialogInterface ->
						dialogInterface.dismiss()
					}
					.show()
				true
			},
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
			}
		)

		(view as RecyclerView).adapter = adapter
		view.layoutManager = linearLayoutManager

		launchViewModelCollectLatest(viewModel.databases) {
			adapter.setList(it)
		}
	}

	private class MyAdapter(
		private var list: List<DatabaseState>,
		private val downloadedOnClickListener: (DatabaseState.DatabaseDownloaded) -> Unit,
		private val onDownloadOnClickListener: (DatabaseAgency) -> Unit,
		private val onDownloadCompletedOnClickListener: () -> Unit,
		private val deleteDownloadedOnLongClickListener: (DatabaseState.DatabaseDownloaded) -> Boolean,
		private val notDownloadedOnClickListener: (DatabaseState.NotDownloaded, LinearProgressIndicator) -> Unit,
	): RecyclerView.Adapter<MyAdapter.ViewHolder>() {

		fun setList(list: List<DatabaseState>) {
			this.list = list
			notifyDataSetChanged()
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			return ViewHolder(
				LayoutInflater.from(parent.context)
					.inflate(R.layout.elem_database_download, parent, false)
			)
		}

		override fun onBindViewHolder(
			holder: ViewHolder,
			position: Int,
		) {
			holder.databaseDownloadName.text = list[position].db.toString()
			when(val data = list[position]) {
				is DatabaseState.DatabaseDownloaded -> {
					holder.progressBar.makeGone()
					if (data.expirationDate.isExpired()) {
						holder.databaseDownloadStateIcon.setImageResource(R.drawable.warning)
						holder.databaseDownloadExpirationDate.text = "Expired since ${data.expirationDate}"
					}
					else {
						holder.databaseDownloadStateIcon.setImageResource(R.drawable.check)
						holder.databaseDownloadExpirationDate.text = "Expires in ${data.expirationDate.cleanString()}"
					}
					holder.databaseDownloadFileSize.makeVisible()
					holder.databaseDownloadFileSize.text = "${data.fileSize} MB"
					holder.databaseDownloadExpirationDate.makeVisible()
					holder.itemView.setOnClickListener{ downloadedOnClickListener(data) }
					holder.itemView.setOnLongClickListener { deleteDownloadedOnLongClickListener(data) }
				}
				is DatabaseState.DatabaseDownloadError, is DatabaseState.DatabaseNotDownloaded -> {
					holder.progressBar.makeGone()
					holder.databaseDownloadStateIcon.setImageResource(R.drawable.error_sign)
					holder.databaseDownloadFileSize.makeGone()
					holder.databaseDownloadExpirationDate.text = "Not installed"
					holder.itemView.setOnClickListener {
						//if user agrees to download the database
						notDownloadedOnClickListener(data, holder.progressBar)
					}
				}

				is DatabaseState.DatabaseDownloading -> {
					holder.databaseDownloadStateIcon.makeGone()
					holder.databaseDownloadFileSize.makeGone()
					holder.progressBar.makeVisible()
					holder.itemView.setOnClickListener {
						onDownloadOnClickListener(data.db)
					}
					when(val progress = data.currentProgress) {
						is NotificationType.DbDownloading -> {
							holder.progressBar.isIndeterminate = false
							holder.databaseDownloadExpirationDate.text = "Downloading..."
							val ratio = progress.current.toFloat() / progress.contentLength.toFloat() * 100
							holder.progressBar.setProgress(ratio.toInt())
						}
						is NotificationType.DbEnqueued -> {
							holder.progressBar.isIndeterminate = true
							holder.databaseDownloadExpirationDate.text = "Starting download..."
						}

						is NotificationType.DbExtracting -> {
							holder.progressBar.isIndeterminate = true
							holder.databaseDownloadExpirationDate.text = "Extracting..."
						}

						is NotificationType.DbUpdateDone -> {
							holder.progressBar.makeGone()
							holder.databaseDownloadStateIcon.makeVisible()
							holder.databaseDownloadStateIcon.setImageResource(R.drawable.baseline_update)
							holder.databaseDownloadExpirationDate.text = "Restart app to see changes"
							holder.itemView.setOnClickListener { onDownloadCompletedOnClickListener() }
						}

						is NotificationType.DbUpdateError -> {
							holder.progressBar.makeGone()
							holder.databaseDownloadStateIcon.makeVisible()
							holder.databaseDownloadStateIcon.setImageResource(R.drawable.error_sign)
							holder.databaseDownloadExpirationDate.text = "Error trying to download shit"
						}
						else -> {}
					}
					//TODO add a cancel download button
//					holder.itemView.setOnClickListener {
//					}
				}

				is DatabaseState.NeedAppRestart -> {
					holder.progressBar.makeGone()
					holder.databaseDownloadStateIcon.makeVisible()
					holder.databaseDownloadStateIcon.setImageResource(R.drawable.baseline_update)
					holder.databaseDownloadExpirationDate.text = "Restart app to see changes"
					holder.itemView.setOnClickListener { onDownloadCompletedOnClickListener() }
				}
			}
		}

		override fun getItemCount(): Int {
			return list.size
		}

		class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			val databaseDownloadName: MaterialTextView = view.findViewById(R.id.settings_database_download_name)
			val databaseDownloadStateIcon: AppCompatImageView = view.findViewById(R.id.settings_database_download_state_image_view)
			val databaseDownloadFileSize: MaterialTextView = view.findViewById(R.id.settings_database_download_file_size)
			val databaseDownloadExpirationDate: MaterialTextView = view.findViewById(R.id.settings_database_download_expiration_date)
			val progressBar: LinearProgressIndicator = view.findViewById(R.id.settings_database_download_progress_bar)
		}

	}
}