package dev.mainhq.bus2go

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.di.CommonModule
import dev.mainhq.bus2go.data.worker.UpdateManagerWorker.Companion.FILE_NAME
import dev.mainhq.bus2go.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

open class Bus2GoApplication : Application() {
	lateinit var commonModule: CommonModule
	lateinit var appModule: AppModule


	override fun onCreate() {
		super.onCreate()

		//check if an apk exists, and delete it if useless
		commonModule = CommonModule(applicationContext)
		appModule = AppModule(applicationContext)

		//FIXME move this shit to the data layer...
		CoroutineScope(Dispatchers.IO).launch {
			TagsHandler.initFile(this@Bus2GoApplication)
		}
		CoroutineScope(Dispatchers.IO).launch {
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
}