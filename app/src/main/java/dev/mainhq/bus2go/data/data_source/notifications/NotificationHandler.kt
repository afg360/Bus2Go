package dev.mainhq.bus2go.data.data_source.notifications

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.mainhq.bus2go.R

class NotificationHandler(private val appContext: Context) {

	companion object{
		private const val APP_UPDATES = "app_updates"
		private const val appUpdateNotifId = 1
		private const val DB_UPDATES = "db_updates"
		private const val dbUpdateNotifId  = 2

		private val notifChannels = mapOf(
			APP_UPDATES to "App Updates",
			DB_UPDATES to "Database Updates",
		)
	}

	private val notificationManager = NotificationManagerCompat.from(appContext)

	private val appNotifChannel = NotificationChannelCompat.Builder(
		APP_UPDATES,
		NotificationManagerCompat.IMPORTANCE_DEFAULT
	).setName(notifChannels[APP_UPDATES])
		.setShowBadge(false)
		.build()

	private val dbNotifChannel = NotificationChannelCompat.Builder(
		DB_UPDATES,
		NotificationManagerCompat.IMPORTANCE_DEFAULT
	).setName(notifChannels[DB_UPDATES])
		.setShowBadge(false)
		.build()

	init {
		notificationManager.createNotificationChannel(appNotifChannel)
		notificationManager.createNotificationChannel(dbNotifChannel)
	}

	/* ---------------------- App Update Notifications ------------------------- */

	fun notifyAppUpdateAvailable(version: String){
		createNotificationBuilder(
			channelId = APP_UPDATES,
			title = "New Release",
			description = "A new version of Bus2Go is available!",
			icon = R.drawable.baseline_update_24,
			priority = NotificationCompat.PRIORITY_DEFAULT
		).setOngoing(false)
			.build()
			.also { postNotif(appUpdateNotifId, it) }
	}

	/** @param percentage A value between 0 to 100 indicating progress. */
	fun notifyAppUpdating(current: Int, contentLength: Int){
		createNotificationBuilder(
			channelId = APP_UPDATES,
			title = "Updating Bus2Go",
			description = "Bus2Go is being updated...",
			icon = R.drawable.baseline_update_24,
			priority = NotificationCompat.PRIORITY_LOW
		).setOngoing(true).setProgress(contentLength, current, false)
			.build()
			.also { postNotif(appUpdateNotifId, it) }
	}

	/** Used to inform the user to tap it to install the new update. */
	fun notifyAppUpdateDone(){
		//val file = File(appContext.cacheDir, )
		//for installing the newly downloaded apk
		val intent = Intent(Intent.ACTION_VIEW).apply {
			//setDataAndType(
			//	FileProvider.getUriForFile(appContext,
			//		"${appContext.packageName}.provider", file),
			//	"application/vnd.android.package-archive"
			//)
		}
		createNotificationBuilder(
			channelId = APP_UPDATES,
			title = "Tap to Install",
			description = "Install the new app version",
			icon = R.drawable.baseline_update_24,
			priority = NotificationCompat.PRIORITY_HIGH
		).setOngoing(true).setProgress(0, 0, false)
			.setContentIntent(
				PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
			)
			.build()
			.also { postNotif(appUpdateNotifId, it) }
		Toast.makeText(appContext, "Update Successful. Go to notifs", Toast.LENGTH_SHORT).show()
	}

	fun notifyAppUpdateFailed(){
		//TODO add a tap to restart/retry
		createNotificationBuilder(
			channelId = APP_UPDATES,
			title = "App Update Error",
			description = "Error trying to update Bus2Go... Tap to retry",
			icon = R.drawable.error_notif_sign,
			priority = NotificationCompat.PRIORITY_HIGH
		).setOngoing(false).setProgress(0, 0, false)
			.build()
			.also { postNotif(appUpdateNotifId, it) }
		Toast.makeText(appContext, "App Update Error", Toast.LENGTH_SHORT).show()
	}

	/* ---------------------- Database Update Notifications ------------------------- */

	/** @param database Is it a bus2go or exo db, or something else */
	fun notifyDbUpdateAvailable(database: String){
		createNotificationBuilder(
			channelId = DB_UPDATES,
			title = "New Database Available",
			description = "A new version of a $database database is available.",
			icon = R.drawable.baseline_update_24,
			priority = NotificationCompat.PRIORITY_DEFAULT
		).setOngoing(false)
			.build()
			.also { postNotif(dbUpdateNotifId, it) }

	}

	fun notifyDbDownloading(current: Int, contentLength: Int){
		createNotificationBuilder(
			channelId = DB_UPDATES,
			title = "Downloading Bus2Go database...",
			description = "Downloading a new version of a Bus2Go database",
			icon = R.drawable.baseline_update_24, //TODO change
			priority = NotificationCompat.PRIORITY_LOW
		).setOngoing(true).setProgress(contentLength, current, false)
			.build()
			.also { postNotif(dbUpdateNotifId, it) }
	}

	fun notifyDbExtracting(){
		createNotificationBuilder(
			channelId = DB_UPDATES,
			title = "Extracting database...",
			description = "",
			icon = R.drawable.baseline_update_24, //TODO change
			priority = NotificationCompat.PRIORITY_DEFAULT
		).setOngoing(true).setProgress(0, 0, true)
			.build()
			.also { postNotif(dbUpdateNotifId, it) }
	}

	fun notifyDbUpdateDone(){
		//FIXME for it to work, need to schedule a restart using alarm manager and broadcast receivers,
		// and then completely shutdown the app using android.os.killProcess()
		val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
		val pendingIntent = PendingIntent.getActivity(
			appContext,
			1234,
			Intent.makeRestartActivityTask(intent?.component),
			PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
		)

		createNotificationBuilder(
			channelId = DB_UPDATES,
			title = "Tap to Restart",
			description = "Tap to restart bus2go to finish the updating of the database.",
			icon = R.drawable.baseline_update_24,
			priority = NotificationCompat.PRIORITY_HIGH
		).setOngoing(false).setProgress(0, 0, false)
			.setContentIntent(pendingIntent)
			.build()
			.also { postNotif(dbUpdateNotifId, it) }
		Toast.makeText(appContext, "Update Successful. Restart App", Toast.LENGTH_SHORT).show()
	}

	//TODO
	fun notifyDbDownloadFailed(){
		createNotificationBuilder(
			channelId = DB_UPDATES,
			title = "Error trying to download database",
			description = "You may try to redownload the database in the settings",
			icon = R.drawable.error_notif_sign, //TODO change
			priority = NotificationCompat.PRIORITY_HIGH
		).setOngoing(false).setProgress(0, 0, false)
			.build()
			.also { postNotif(dbUpdateNotifId, it) }
		Toast.makeText(appContext, "Database Download Error", Toast.LENGTH_SHORT).show()
	}

	//TODO other possible notifications...?

	//FIXME internally deal with id instead...
	private fun postNotif(id: Int, notification: Notification){
		if (ActivityCompat.checkSelfPermission(
				appContext,
				Manifest.permission.POST_NOTIFICATIONS
			) == PackageManager.PERMISSION_GRANTED
		) notificationManager.notify(id, notification)
	}

	/** Sets up a builder for setting up the content title, description, small icon and prio. */
	private fun createNotificationBuilder(
		channelId: String,
		title: String,
		description: String,
		icon: Int,
		priority: Int,
	) : NotificationCompat.Builder {
		return NotificationCompat.Builder(appContext, channelId)
			.setContentTitle(title)
			.setContentText(description)
			.setSmallIcon(icon)
			.setPriority(priority)
	}
}