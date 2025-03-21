package dev.mainhq.bus2go

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import dev.mainhq.bus2go.di.AppContainer
import dev.mainhq.bus2go.workers.UpdateManagerWorker.Companion.FILE_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BaseApplication : Application() {
	private lateinit var appContainer: AppContainer

	override fun onCreate() {
		super.onCreate()
		appContainer = AppContainer(applicationContext)
		//check if an apk exists, and delete it if useless
		MainScope().launch {
			cleanUp()
		}

	}

	private suspend fun cleanUp(){
		val file = withContext(Dispatchers.IO) {
			File(applicationContext.cacheDir, FILE_NAME)
		}

		if (file.exists()) {
			try{
				val packageInfo: PackageInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
				val versionCode = if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
				val apkPackageInfo: PackageInfo? = applicationContext.packageManager.getPackageArchiveInfo("${applicationContext.cacheDir}/$FILE_NAME", PackageManager.GET_META_DATA)
				if (apkPackageInfo == null){
					Log.d("UPDATES", "File exists but package manager couldnt find it...?")
				}
				else {
					val apkVersionCode =
						if (Build.VERSION.SDK_INT >= 28) apkPackageInfo.longVersionCode else apkPackageInfo.versionCode.toLong()
					if (versionCode >= apkVersionCode) {
						Log.d("UPDATES", "Useless file detected. Deleting")
						file.delete()
					}
				}
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