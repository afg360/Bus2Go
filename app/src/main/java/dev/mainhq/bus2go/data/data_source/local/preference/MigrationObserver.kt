package dev.mainhq.bus2go.data.data_source.local.preference

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/**
 * An observer that makes sure that both dataStores are initialised and have migrated before
 * deleting old files.
 **/
object MigrationObserver {
	private var shouldCleanUpStm = MutableStateFlow(false)
	private var shouldCleanUpExo = MutableStateFlow(false)
	private val mutex = Mutex()

	fun notifyStmCleanUpReady(context: Context){
		shouldCleanUpStm.value = true
		if (shouldCleanUpExo.value) cleanUp(context)
	}

	fun notifyExoCleanUpReady(context: Context){
		shouldCleanUpExo.value = true
		if (shouldCleanUpStm.value) cleanUp(context)
	}

	private fun cleanUp(context: Context){
		CoroutineScope(Dispatchers.IO).launch {
			mutex.withLock {
				val favouritesV1 = File(context.filesDir.resolve("datastore"), "favourites.json")
				if (favouritesV1.exists()) favouritesV1.delete()
				val favouritesV2 = File(context.filesDir.resolve("datastore"), "favourites_1.json")
				if (favouritesV2.exists()) favouritesV2.delete()
			}
		}
	}
}