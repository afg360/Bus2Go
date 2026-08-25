package dev.mainhq.bus2go.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.DatabaseState
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import dev.mainhq.bus2go.domain.use_case.ObserveDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.db_state.DeleteDatabase
import io.ktor.util.Hash.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/** Should be an activity ViewModel since it should still run in the background */
class SettingsDownloadDatabasesFragmentViewModel(
	private val observeDownloadDatabaseTask: ObserveDownloadDatabaseTask,
	private val scheduleDownloadDatabaseTask: ScheduleDownloadDatabaseTask,
	private val deleteDatabase: DeleteDatabase
): ViewModel() {

	@OptIn(ExperimentalCoroutinesApi::class)
	val databases = observeDownloadDatabaseTask.invoke().stateIn(
		viewModelScope,
		SharingStarted.WhileSubscribed(5000),
		emptyList()
	)

//	@OptIn(ExperimentalCoroutinesApi::class)
//	val databases = combine(_databases, _jobs) { databases, jobIds ->
//		databases to jobIds
//	}.flatMapLatest { (databases, jobIds) ->
//		if (jobIds.isEmpty()) {
//			flowOf(databases)
//		}
//		else {
//			combine(
//				jobIds.map { (agency, uuid) ->
//					observeDownloadDatabaseTask.invoke(uuid).map {
//						agency to it
//					}
//				}
//			) { notificationPairs ->
//				val notifs = notificationPairs.associate { (agency, notification) ->
//					agency to DatabaseState.DatabaseDownloading(agency, notification)
//				}
//
//				//this is needed to remap databases with no jobIds associated with them
//				databases.map {
//					notifs[it.db] ?: it
//				}
//			}
//		}
//	}.combine(_downloadsDone) { databases, downloadsDone ->
//		databases.map { database ->
//			downloadsDone.find { downloadsDone.contains(it) }?.let {
//				DatabaseState.NeedAppRestart(it)
//			} ?: database
//		}
//	}.stateIn(
//		viewModelScope,
//		SharingStarted.WhileSubscribed(5000),
//		emptyList()
//	)

	fun downloadDatabase(databaseAgency: DatabaseAgency) {
		viewModelScope.launch {
			scheduleDownloadDatabaseTask.invoke(listOf(databaseAgency))
//			_jobs.update { jobs ->
//				jobs.toMutableMap().also { map ->
//					map.putAll(
//						scheduleDownloadDatabaseTask.invoke(listOf(databaseAgency)).map {
//							databaseAgency to it
//						}
//					)
//				}
//			}
		}
	}

	fun deleteDatabase(databaseAgency: DatabaseAgency) {
		viewModelScope.launch {
			deleteDatabase.invoke(databaseAgency)
		}
	}

	fun updateDatabase(databaseAgency: DatabaseAgency) {
		viewModelScope.launch {
			//TODO is it even needed...?
		}
	}

}