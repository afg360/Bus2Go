package dev.mainhq.bus2go

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import dev.mainhq.bus2go.data.worker.UpdateManagerWorker.Companion.FILE_NAME
import kotlinx.coroutines.launch
import java.io.File

class Bus2GoApplication(): BaseBus2GoApplication() {

	override fun onCreate() {
		super.onCreate()
		manageApkFiles()
	}

	//TODO move this code to data layer and use useCases instead, and should only be used in the indep flavour
	private fun manageApkFiles(){
		//check if an apk of the app exists in the cache. if it does and current version is newer, delete older version
		coroutineScope.launch {
			val file = File(applicationContext.cacheDir, FILE_NAME)

			if (file.exists()) {
				try{
					val packageInfo = applicationContext.packageManager
						.getPackageInfo(packageName, 0)
					@Suppress("DEPRECATION")
					val versionCode = if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode
					else packageInfo.versionCode.toLong()

					applicationContext.packageManager
						.getPackageArchiveInfo(
							"${applicationContext.cacheDir}/$FILE_NAME",
							PackageManager.GET_META_DATA
						)?.also {
							@Suppress("DEPRECATION")
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
}