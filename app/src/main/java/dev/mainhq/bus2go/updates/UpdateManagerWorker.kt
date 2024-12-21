package dev.mainhq.bus2go.updates

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.R
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


class UpdateManagerWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams ) {

	companion object {
		const val TAG = "CHECK-FOR-UPDATES"
	}

	override suspend fun doWork(): Result {
		//temporarily, fetch new releases from github releases bus2go page
		//api endpoint for all releases
		val githubReleases = "https://api.github.com/repos/afg360/bus2go/releases"

		val client = HttpClient(OkHttp){}
		val httpResponse: HttpResponse = client.get(githubReleases) {
			headers {
				append(HttpHeaders.Accept, "application/vnd.github+json")
			}
		}
		when (httpResponse.status.value) {
			in 200..299 -> {
				Log.d("UPDATES-FETCHING", "Fetched succesfully github")
				//we are getting the latest release,
				//FIXME includes pre-releases, for development...
				val latestRelease = Json.decodeFromString(JsonArray.serializer(), httpResponse.body()).toList()[0].jsonObject
				//check tag name, which is essentially version name
				val tagVersion = VersionName(latestRelease["tag_name"]?.jsonPrimitive?.contentOrNull ?: throw IllegalStateException("Tag name seems to not exist? Either wrong parsing or something shitty happened..."))
				val localVersionName = VersionName(applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
					.versionName ?: throw IllegalStateException("Cannot have a nulled value version name!"))

				val channelId = "Updates"
				val notificationManager: NotificationManager =
					applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					val channel = NotificationChannel( channelId, "Software Update", NotificationManager.IMPORTANCE_DEFAULT )
					notificationManager.createNotificationChannel(channel)
				}

				if (localVersionName < tagVersion){
					Log.d("UPDATES", "Seems that you need to update!")

					val notification = NotificationCompat.Builder(applicationContext, channelId)
						.setSmallIcon(R.drawable.bus2go_dark_ic_launcher_foreground)
						.setContentTitle("Updates")
						.setContentText("New update available: $tagVersion")
						.setPriority(NotificationCompat.PRIORITY_DEFAULT)
						.build()

					notificationManager.notify(1, notification)
				}
				else{
					Log.d("UPDATES", "Version seems to be up to date")
					//FIXME for testing only, at release comment this
					/*
					val notification = NotificationCompat.Builder(applicationContext, channelId)
						.setSmallIcon(R.drawable.bus2go_dark_ic_launcher_foreground)
						.setContentTitle("Updates")
						.setContentText("No new updates found")
						.setPriority(NotificationCompat.PRIORITY_DEFAULT)
						.build()

					notificationManager.notify(1, notification)
					 */
				}
				return Result.success()
			}
			else -> {
				Log.e("UPDATES", "Error fetching github for new releases. Be sure you are connected to a network and haven't reached the rate limit")
				return Result.failure()
			}
		}

		//for latest, DOES NOT INCLUDE PRE-RELEASES
		//https://api.github.com/repos/afg360/bus2go/releases/latest

	}

	private class VersionName(name : String){
		private val mainVersion: Int
		private val subVersion: Int
		private val subSubVersion: Int
		private val isPreRelease: Boolean

		init{
			//format expected: v<main-version>.<subVersion>.<subSubVersion>[-{alpha}|{beta}]
			val data = name.substring(1) .split(Regex("[.-]"))
			mainVersion = data[0].toInt()
			subVersion = data[1].toInt()
			subSubVersion = data[2].toInt()
			isPreRelease = data[3] == "alpha" || data[3] ==  "beta"
		}

		operator fun compareTo(other: VersionName): Int{
			return if (mainVersion < other.mainVersion ) -1
			else if (mainVersion > other.mainVersion) 1
			else {
				if (subVersion < other.subVersion) -1
				else if (subVersion > other.subVersion) 1
				else{
					if (subSubVersion < other.subSubVersion) -1
					else if (subSubVersion > other.subSubVersion) 1
					else {
						//v1.1.0-alpha < v1.1.0 by definition
						if ((isPreRelease && other.isPreRelease) || (!isPreRelease && !other.isPreRelease)) 0
						else if (isPreRelease) -1
						else 1
					}
				}
			}
		}

		override fun toString(): String {
			var str = "v$mainVersion.$subVersion.$subSubVersion"
			if (isPreRelease) str += "-PreRelease"
			return str
		}
	}
}