package dev.mainhq.bus2go

import android.app.Application
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import dev.mainhq.bus2go.di.CommonModule
import dev.mainhq.bus2go.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

abstract class BaseBus2GoApplication : Application() {
	lateinit var commonModule: CommonModule
	lateinit var appModule: AppModule

	//Supervisor job allows the coroutineScope to not cancel all of its child coroutines when one fails
	//Using a single one to minimise memory and be able to terminate it when needed
	protected val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	override fun onCreate() {
		super.onCreate()

		appModule = AppModule(applicationContext)
		commonModule = CommonModule(applicationContext, appModule.loggerImpl)

		//TODO move logic to use case classes
		initTagsFiles()
		initNetworkClient()
		resetAppNeedRestartState()

		applicationScope.launch {
			commonModule.cleanUpGarbageFiles()
		}
	}

	private fun initTagsFiles(){
		applicationScope.launch {
			TagsHandler.initFile(this@BaseBus2GoApplication)
		}
	}

	private fun initNetworkClient(){
		applicationScope.launch {
			NetworkClient.init(commonModule.customTrustManager)
		}
	}

	private fun resetAppNeedRestartState() {
		applicationScope.launch {
			commonModule.appStateRepository.resetRestartNeededFlag()
		}
	}


	override fun onLowMemory() {
		applicationScope.cancel("Low Memory")
		super.onLowMemory()
	}

	override fun onTerminate() {
		applicationScope.cancel("App terminated")
		super.onTerminate()
	}
}