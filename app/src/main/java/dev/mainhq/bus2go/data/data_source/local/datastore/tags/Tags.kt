package dev.mainhq.bus2go.data.data_source.local.datastore.tags

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Class that manages a list of tags used by favourites. Using a separate file to store these so that
 * it is easier to see which tags are used
 **/
class Tags(private val applicationContext: Context) {
	private val filename = "tags.txt"
	private val mutex = Mutex()

	suspend fun readTags(): List<String> {
		return mutex.withLock {
			withContext(Dispatchers.IO) {
				try {
					applicationContext.openFileInput(filename).bufferedReader().useLines { lines ->
						lines.filter { it.isNotEmpty() }.toList()
					}
				}
				catch (_: Exception) {
					emptyList()
				}
			}
		}
	}

	@Throws(IllegalStateException::class)
	/** Adds a new tag to the file. If it already exists, simply ignored */
	suspend fun addTag(tag: String) {
		if (tag.isBlank()) throw IllegalStateException("Disallow use of blank tags in higher level of code")
		val tags = readTags().sorted().toMutableSet().apply { add(tag) }
		mutex.withLock {
			withContext(Dispatchers.IO){
				applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use { fileOutputStream ->
					tags.forEach {
						fileOutputStream.write("$it\n".toByteArray())
					}
				}
			}
		}
	}

	@Throws(IllegalStateException::class)
	suspend fun removeTag(tag: String) {
		if (tag.isBlank()) throw IllegalStateException("Disallow use of blank tags in higher level of code")
		val tags = readTags().filter { it != tag }
		mutex.withLock {
			withContext(Dispatchers.IO){
				applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use { fileInputStream ->
					tags.forEach {
						if (it.isNotEmpty()){
							fileInputStream.write("$it\n".toByteArray())
						}
					}
				}
			}
		}
	}

}