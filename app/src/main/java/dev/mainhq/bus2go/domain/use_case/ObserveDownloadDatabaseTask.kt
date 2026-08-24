package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.backgroundtask.DatabaseDownloadScheduler
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.entity.DatabaseState
import dev.mainhq.bus2go.domain.entity.NotificationType
import dev.mainhq.bus2go.domain.repository.AppStateRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.UUID

class ObserveDownloadDatabaseTask(
	private val appStateRepository: AppStateRepository,
	private val databaseDownloadScheduler: DatabaseDownloadScheduler
){

	@OptIn(ExperimentalCoroutinesApi::class)
	operator fun invoke(): Flow<List<DatabaseState>> {
		return appStateRepository.databases.flatMapLatest { list ->
			combine(
				list.map { databaseState ->
					combine(
						appStateRepository.databaseWorkNameState,
						databaseDownloadScheduler.observeWork(databaseState.db.name.uppercase())
					) { workNameState, workInfo ->
						(workNameState[databaseState.db] ?: false) to workInfo
					}.map { (needsAppUpdate, workInfo) ->
						databaseState to when(workInfo) {
							//TODO when app last state was failed, it will stay like that for now
							// we'll see if we want to add logic or not similar to app restart needed
							is NotificationType.DbUpdateDone -> {
								//needed to not trigger a restart app when the app was already updated
								if (needsAppUpdate) {
									workInfo
								}
								else {
									null
								}
							}
							else -> workInfo
						}
					}
				}
			) { pairs ->
				pairs.map { (databaseState, notif) ->
					notif?.let {
						DatabaseState.DatabaseDownloading(
							databaseState.db,
							it
						)
					} ?: databaseState
				}
			}
		}
	}
}