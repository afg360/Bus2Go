package dev.mainhq.bus2go.updates

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.R
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File


class UpdateManagerWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams ) {

	companion object {
		const val TAG = "CHECK-FOR-UPDATES"
		const val NOTIF_ID = 1
		const val FILE_NAME = "bus2go-update.apk"
	}

	//for latest, DOES NOT INCLUDE PRE-RELEASES
	//https://api.github.com/repos/afg360/bus2go/releases/latest

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
				val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					val channel = NotificationChannel(channelId, "Software Updates", NotificationManager.IMPORTANCE_DEFAULT)
					notificationManager.createNotificationChannel(channel)
				}

				if (localVersionName < tagVersion){
					Log.d("UPDATES", "Seems that you need to update!")
					val isAutoUpdate = PreferenceManager.getDefaultSharedPreferences(applicationContext)
						.getBoolean("auto-updates", false)
					//if autoUpdate is not on, notification click will send to browser -> website where to download
					//(for now github, eventually server or even fdroid managed)

					//FIXME for now we have bus2go-debug.apk as names...
					val appRegexName = Regex("bus2go-debug.*\\.apk")
					val downloadLink = (latestRelease["assets"] as JsonArray)
						//perhaps no need to map, since latest would be the first element?
						.map { it as JsonObject }
						.find{ it["content_type"]?.jsonPrimitive?.content == "application/vnd.android.package-archive"
								&& it["name"]?.jsonPrimitive?.content?.contains(appRegexName) == true
						}?.get("url") ?: throw IllegalStateException("Expected a non null value for the github download link")

					if (isAutoUpdate){
						//before dowloading, check if a correct apk already exists, and jump to installation
						//if already there...
						val notification = NotificationCompat.Builder(applicationContext, channelId)
							.setContentTitle("Auto Update")
							.setContentText("Auto updating Bus2Go...")
							.setSmallIcon(R.drawable.baseline_update_24)
							//.setSmallIcon(R.drawable.bus2go_dark_ic_launcher_foreground)
							.setPriority(NotificationCompat.PRIORITY_DEFAULT)
							.build()
						notificationManager.notify(NOTIF_ID, notification)

						autoUpdate(client, downloadLink.jsonPrimitive.content, notificationManager, channelId)
					}
					else{
						val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadLink.jsonPrimitive.content))
						val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

						val notification = NotificationCompat.Builder(applicationContext, channelId)
							.setContentTitle("Bus2Go $tagVersion now available")
							.setContentText("Tap to download the latest version")
							.setSmallIcon(R.drawable.baseline_update_24)
							//.setSmallIcon(R.drawable.bus2go_dark_ic_launcher_foreground)
							.setPriority(NotificationCompat.PRIORITY_DEFAULT)
							.setContentIntent(pendingIntent)
							.build()

						notificationManager.notify(NOTIF_ID, notification)
					}
				}
				else{
					//FIXME for testing only, at release comment this
					Log.d("UPDATES", "Version seems to be up to date")
					/*
					val downloadLink = "https://github.com/afg360/Bus2Go/releases/download/v1.1.0-alpha/bus2go-debug.apk"
					val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadLink))
					val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

					val notification = NotificationCompat.Builder(applicationContext, channelId)
						.setSmallIcon(R.drawable.bus2go_dark_ic_launcher_foreground)
						.setContentTitle("Bus2Go $tagVersion now available")
						.setContentText("Tap to download the latest version")
						.setPriority(NotificationCompat.PRIORITY_DEFAULT)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true)
						.build()

					notificationManager.notify(NOTIF_ID, notification)
					autoUpdate(client, downloadLink, notificationManager, channelId)
					 */
				}
				return Result.success()
			}
			else -> {
				Log.e("UPDATES", "Error fetching github for new releases. Be sure you are connected to a network and haven't reached the rate limit")
				return Result.failure()
			}
		}
	}

	private suspend fun autoUpdate(client: HttpClient, downloadLink : String, notificationManager: NotificationManager, channelId: String){
		val file = withContext(Dispatchers.IO) {
			File(applicationContext.cacheDir, FILE_NAME)
		}

		//do not redownload apk if already downloaded correctly
		if (!file.exists()){
			val tmpFile = withContext(Dispatchers.IO){
				File(applicationContext.cacheDir, "$FILE_NAME.part")
			}

			val updatedNotificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
				.setContentTitle("Downloading file")
				.setContentText("Downloading in progress...")
				.setSmallIcon(R.drawable.baseline_update_24)
				//.setSmallIcon(R.drawable.bus2go_dark_ic_launcher_foreground)
				.setOngoing(true) //since dont want to remove notif from downloading process

			//TODO handle requests when not connected to internet, or sudden loss of connection
			//if correctly downloaded (maybe by checking check sum), then no need to redownload?
			try {
				client.prepareGet(downloadLink).execute { httpResponse ->
					val channel: ByteReadChannel = httpResponse.body()
					var downloadedSize: Long = 0
					var lastProgress = 0
					val threshold = 3
					while (!channel.isClosedForRead) {
						val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
						while (!packet.isEmpty) {
							val bytes = packet.readBytes()
							tmpFile.appendBytes(bytes)

							downloadedSize += bytes.size
							val progress =
								(channel.totalBytesRead * 100 / httpResponse.contentLength()!!).toInt()
							if (progress >= lastProgress + threshold) {
								lastProgress = progress
								val updatedNotification = updatedNotificationBuilder
									.setProgress(100, progress, false)
									.build()

								notificationManager.notify(NOTIF_ID, updatedNotification)
							}
						}
					}
				}
				tmpFile.renameTo(file)
			}
			catch (ioE: IOException){
				//TODO finish handling of IO errors
				Log.e("UPDATE", "IO exception during downloading")
				Toast.makeText(applicationContext, "Error during update download", Toast.LENGTH_SHORT).show()
				//delete tmp file?
				tmpFile.delete()
				return
			}
		}


		//compare checksums for security purposes?
		//FIXME
		notificationManager.cancel(NOTIF_ID)
		val intent = Intent(Intent.ACTION_VIEW)
		intent.setDataAndType(
			FileProvider.getUriForFile(applicationContext,
				applicationContext.packageName + ".provider", file),
			"application/vnd.android.package-archive"
		)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
		//applicationContext.sendBroadcast(intent)
		val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

		//delete the downloaded file...
		//remove the notif and replace it with this
		val updatedNotification = NotificationCompat.Builder(applicationContext, channelId)
			.setContentTitle("Tap to install Bus2Go")
			.setContentText("Install the downloaded apk")
			.setOngoing(true)
			.setSmallIcon(R.drawable.baseline_update_24)
			//.setSmallIcon(R.drawable.bus2go_dark_ic_launcher_foreground)
			.setContentIntent(pendingIntent)
			.setFullScreenIntent(pendingIntent, true)
			.setAutoCancel(true)
			.build()
		notificationManager.notify(NOTIF_ID, updatedNotification)

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