package dev.mainhq.bus2go

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.data.data_source.remote.NetworkClient
import dev.mainhq.bus2go.di.CommonModule
import dev.mainhq.bus2go.data.worker.UpdateManagerWorker.Companion.FILE_NAME
import dev.mainhq.bus2go.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

open class Bus2GoApplication : Application() {
	lateinit var commonModule: CommonModule
	lateinit var appModule: AppModule

	//Supervisor job allows the coroutineScope to not cancel all of its child coroutines when one fails
	//Using a single one to minimise memory and be able to terminate it when needed
	private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	override fun onCreate() {
		super.onCreate()

		//check if an apk exists, and delete it if useless
		commonModule = CommonModule(applicationContext)
		appModule = AppModule(applicationContext)

		//TODO move logic to use case classes
		initTagsFiles()
		initNetworkClient()
		manageApkFiles()

		coroutineScope.launch {
			commonModule.cleanUpGarbageFiles()
		}
	}

	private fun initTagsFiles(){
		coroutineScope.launch {
			TagsHandler.initFile(this@Bus2GoApplication)
		}
	}

	private fun initNetworkClient(){
		coroutineScope.launch {
			NetworkClient.init(filesDir)
		}
	}

	//TODO move this code to data layer and use useCases instead
	private fun manageApkFiles(){
		//check if an apk of the app exists in the cache. if it does and current version is newer, delete older version
		coroutineScope.launch {
			val file = File(applicationContext.cacheDir, FILE_NAME)

			if (file.exists()) {
				try{
					val packageInfo = applicationContext.packageManager
						.getPackageInfo(packageName, 0)
					val versionCode = if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode
					else packageInfo.versionCode.toLong()

					applicationContext.packageManager
						.getPackageArchiveInfo(
							"${applicationContext.cacheDir}/$FILE_NAME",
							PackageManager.GET_META_DATA
						)?.also {
							val apkVersionCode = if (Build.VERSION.SDK_INT >= 28) it.longVersionCode
							else it.versionCode.toLong()

							if (versionCode >= apkVersionCode) {
								Log.d("UPDATES", "Useless file detected. Deleting")
								file.delete()
							}
						} ?: Log.d("UPDATES", "File exists but package manager couldnt find it...?")

					//TODO At Launch, check if junk files exist (e.g. database compressed archives)
					// if they do, check if the database version is up to date (i.e. superior or equal
					// to the one stored. If yes, then delete the piece of junk
				}
				catch (e: Exception){
					e.printStackTrace()
				}
			}
			else{
				Log.d("UPDATES", "No garbage apk detected")
			}
		}
	}

	override fun onLowMemory() {
		coroutineScope.cancel("Low Memory")
		super.onLowMemory()
	}

	override fun onTerminate() {
		coroutineScope.cancel("App terminated")
		super.onTerminate()
	}
}