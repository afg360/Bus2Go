package dev.mainhq.bus2go.workers

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.workers.UpdateManagerWorker.Companion.NOTIF_ID
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Worker downloading the selected databases at configuration time. */
class DatabaseDownloadManagerWorker(context: Context, workerParams: WorkerParameters ) : CoroutineWorker(context, workerParams) {

	companion object{
		private const val STM_URL = "https://www.stm.info/sites/default/files/gtfs/gtfs_stm.zip"
		private const val STM_FILE = "gtfs_stm.zip"
		private const val STM_DB_NOTIF_CHANNEL_ID = "STM database"
		private const val EXO_URL = "https://exo.quebec/xdata"
		private const val EXO_FILE = "google_transit.zip"
		private const val EXO_DB_NOTIF_CHANNEL_ID = "EXO database"

	}

	private val client = HttpClient(OkHttp){}
	private lateinit var notificationManager: NotificationManager

	override suspend fun doWork(): Result {
		//check which databases to download
		val isStm = inputData.getBoolean("stm", false)
		val isExo = inputData.getBoolean("exo", false)
		val jobs: MutableList<Deferred<Boolean>> = mutableListOf()
		notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		Log.d("DB WORKER", "Started db download work")
		//FIXME downloading initiates but does not seem to save files properly...?
		if (withContext(Dispatchers.IO){
			if (isStm) jobs.add(async{return@async downloadStm()})
			if (isExo) downloadAllExo(this, jobs)

			return@withContext jobs.map { it.await() }.reduce{b1, b2 -> b1 && b2}
		})
			return Result.success()
		else return Result.failure()
	}

	private suspend fun downloadStm(): Boolean{
		val stmFile = File(applicationContext.filesDir.resolve("stm"), STM_FILE)
		//if it exists, cancel downloading since it already happened (unless need an update?)
		if (stmFile.exists()) return true
		val tmpFile = File(applicationContext.filesDir.resolve("stm"), "$STM_FILE.part")
		return downloadStream(STM_URL, tmpFile, stmFile,
			NotificationCompat.Builder(applicationContext, STM_DB_NOTIF_CHANNEL_ID)
				.setSmallIcon(R.drawable.baseline_language_24)
				.setContentTitle("Stm db download")
				.setContentText("Downloading stm db")
		)
	}

	private fun downloadAllExo(coroutineScope: CoroutineScope, jobs: MutableList<Deferred<Boolean>>){
		//TODO could setup a fancy way of checking which exo were correctly installed and which were not
		val exoAgencies = listOf(
			//"citcrc",   //autos Chambly-Richelieu-Carignan
			//"cithsl",   //autos Haut-Saint-Laurent
			//"citla",    //autos Laurentides
			//"citpi",    //autos La Presqu'île
			//"citlr",    //autos Le Richelain
			//"citrous",  //autos Roussillon
			//"citsv",    //autos Sorel-Varennes
			//"citso",    //autos Sud-ouest
			//"citvr",    //autos Vallée du Richelieu
			//"mrclasso", //autos L'Assomption
			//"mrclm",    //autos Terrebonne-Mascouche
			"trains",
			"omitsju",  //autos Sainte-Julie
			"lrrs"      //autos Le Richelain et Roussillon
		)
		exoAgencies.forEach { jobs.add(coroutineScope.async{return@async downloadExoAgency(it)}) }
	}

	private suspend fun downloadExoAgency(agency: String): Boolean{
		//instead of checking the files, first check if the dir exists
		val agencyDir = File(applicationContext.filesDir.resolve("exo"), agency)
		//if it exists, cancel downloading since it already happened (unless need an update?)
		if (!agencyDir.exists()) {
			if (!agencyDir.mkdirs()){
				Log.e("Exo Db Installation", "Error trying to create the exo $agency directory")
				throw IOException("Error trying to create Exo $agency directory...")
			}
		}
		assert(agencyDir.isDirectory)
		val agencyZipFile = File(applicationContext.filesDir.resolve("exo/$agency"), EXO_FILE)
		if (agencyZipFile.exists()) return true

		val tmpFile = File(applicationContext.filesDir.resolve("exo/$agency"), "$EXO_FILE.part")
		return downloadStream("$EXO_URL/$agency/$EXO_FILE", tmpFile, agencyZipFile,
			NotificationCompat.Builder(applicationContext, EXO_DB_NOTIF_CHANNEL_ID)
				.setSmallIcon(R.drawable.baseline_language_24)
				.setContentTitle("Exo db Download")
				.setContentText("Downloading Exo databases"))
	}

	private suspend fun downloadStream(url: String, tmpFile: File, file: File, notifBuilder: NotificationCompat.Builder): Boolean{
		return client.prepareGet(url).execute {
			val channel: ByteReadChannel = it.body()
			var downloadedSize: Long = 0
			var lastProgress = 0
			val threshold = 3

			try {
				Log.d("DB Download", "Started db stream download: $url")
				while (!channel.isClosedForRead) {
					val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
					while (!packet.isEmpty) {
						val bytes = packet.readBytes()
						tmpFile.appendBytes(bytes)

						downloadedSize += bytes.size
						val progress =
							(channel.totalBytesRead * 100 / it.contentLength()!!).toInt()
						if (progress >= lastProgress + threshold) {
							lastProgress = progress
							val updatedNotification = notifBuilder
								.setProgress(100, progress, false)
								.build()

							withContext(Dispatchers.Main) {
								notificationManager.notify(NOTIF_ID, updatedNotification)
							}
						}
					}
				}
				tmpFile.renameTo(file)
				Log.d("DB Download", "Finished downloading...")
				return@execute true
			}

			catch (ioE: IOException){
				//TODO finish handling of IO errors
				Log.e("DATABASE DOWNLOAD", "IO exception during downloading")
				Toast.makeText(applicationContext, "Error during database download", Toast.LENGTH_SHORT).show()
				//delete tmp file?
				tmpFile.delete()
				return@execute false
			}
		}
	}
}