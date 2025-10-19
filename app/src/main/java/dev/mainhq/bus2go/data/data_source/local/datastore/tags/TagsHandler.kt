package dev.mainhq.bus2go.data.data_source.local.datastore.tags

import android.content.Context
import dev.mainhq.bus2go.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Class that manages a list of tags used by favourites. Using a separate file to store these so that
 * it is easier to see which tags are used
 **/
class TagsHandler private constructor(private val applicationContext: Context) {

	companion object {
		private const val filename = "tags.csv"
		private val mutex = Mutex()
		private var INSTANCE: TagsHandler? = null
		private const val MAX_CHAR_SIZE = 30

		fun getInstance(applicationContext: Context): TagsHandler {
			return INSTANCE ?: TagsHandler(applicationContext).also { INSTANCE = it }
		}

		/**
		 * Only called by the application on launch when needed.
		 * @return If the operation has succeeded or not
		 * */
		suspend fun initFile(applicationContext: Context): Boolean {
			return mutex.withLock {
				withContext(Dispatchers.IO) {
					val file = File(applicationContext.filesDir, filename)
					try {
						if (!file.exists()) {
							file.createNewFile()
							file.writeText(
								"Stm,${applicationContext.resources.getColor(R.color.basic_blue, null).toString(16)}\n" +
										"Exo,${applicationContext.resources.getColor(R.color.basic_purple, null).toString(16)}\n" +
										"Train,${applicationContext.resources.getColor(R.color.orange, null).toString(16)}\n")
						}
						true
					}
					catch (_: SecurityException) {
						false
					}
					catch (_: IOException) {
						false
					}
				}
			}
		}
	}

	suspend fun readTags(): List<TagDto> {
		return mutex.withLock {
			withContext(Dispatchers.IO) {
				try {
					applicationContext.openFileInput(filename).bufferedReader().useLines { lines ->
						lines.filter { it.isNotEmpty() }.toList().map { line ->
							line.split(",").let {
								//TODO: parse the color saved in hexa format
								TagDto(it[0], it[1].toInt(16))
							}
						}
					}
				}
				catch (_: Exception) {
					emptyList()
				}
			}
		}
	}

	@Throws(IllegalStateException::class, IllegalArgumentException::class)
	/** Adds a new tag to the file. If it already exists, simply ignored */
	suspend fun addTag(tag: TagDto) {
		if (tag.label.isBlank()) throw IllegalStateException("Disallow use of blank tags in higher level of code")
		if (tag.label.contains(',')) throw IllegalArgumentException("Tag shouldn't contain commas")
		if (tag.label.length > MAX_CHAR_SIZE ) throw IllegalArgumentException("Tag shouldn't be more than $MAX_CHAR_SIZE character long")

		val tags = readTags().sorted().toMutableSet().apply { add(tag) }
		mutex.withLock {
			withContext(Dispatchers.IO){
				applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use { fileOutputStream ->
					tags.forEach {
						fileOutputStream.write("${it.label},${it.color}\n".toByteArray())
					}
				}
			}
		}
	}

	@Throws(IllegalStateException::class)
	suspend fun removeTag(tag: TagDto) {
		if (tag.label.isBlank()) throw IllegalStateException("Disallow use of blank tags in higher level of code")
		val tags = readTags().filter { it != tag }
		mutex.withLock {
			withContext(Dispatchers.IO){
				applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use { fileInputStream ->
					tags.forEach {
						if (it.label.isNotEmpty()){
							fileInputStream.write("${it.label},${it.color}\n".toByteArray())
						}
					}
				}
			}
		}
	}

}