package dev.mainhq.bus2go.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.mainhq.bus2go.domain.entity.Time
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import dev.mainhq.bus2go.domain.repository.TransitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Throws


/** Assumes that the [Long] in question is in milliseconds. */
fun Long.toEpochDay(): Long {
	return this / (3600 * 1000 * 24)
}

fun LocalDate.toEpochMillis(): Long {
	return this.toEpochDay() * 24 * 3600 * 1000
}

/** Check if the [LocalDate] is lesser than today. */
fun LocalDate.isExpired(): Boolean {
	return this < LocalDate.now()
}

/** @return A [String] of the form Jan 01, 1970. */
fun LocalDate.cleanString(): String {
	return format(DateTimeFormatter.ofPattern("MMM dd, uuuu"))
}

fun Instant.toTime(): Time {
	return Time.fromMillis(this.toEpochMilli())
}

fun View.makeVisible(){
	this.visibility = View.VISIBLE
}

fun View.makeInvisible(){
	this.visibility = View.INVISIBLE
}

fun View.makeGone(){
	this.visibility = View.GONE
}


fun <T> AppCompatActivity.launchViewModelCollectLatest(flow: Flow<T>, context: CoroutineContext = Dispatchers.Main, block: suspend (T) -> Unit): Job {
	return lifecycleScope.launch(context) {
		lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
			flow.collectLatest(block)
		}
	}
}

fun <T> AppCompatActivity.launchViewModelCollectLatest(flow: Flow<T>, context: CoroutineContext = Dispatchers.Main, lifecycleState: Lifecycle.State = Lifecycle.State.STARTED, block: suspend (T) -> Unit): Job {
	return lifecycleScope.launch(context) {
		lifecycle.repeatOnLifecycle(lifecycleState){
			flow.collectLatest(block)
		}
	}
}

fun <T> Fragment.launchViewModelCollectLatest(flow: Flow<T>, context: CoroutineContext = Dispatchers.Main, block: suspend (T) -> Unit): Job {
	return viewLifecycleOwner.lifecycleScope.launch(context) {
		viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
			flow.collectLatest(block)
		}
	}
}

fun <T> MutableList<T>.swap(oldPosition: Int, newPosition: Int) {
	val oldItem = this[newPosition]
	this[newPosition] = this[oldPosition]
	this[oldPosition] = oldItem
}

/** @throws IllegalStateException When the list does not contain the repo of the transitType. */
@Throws(IllegalStateException::class)
fun Iterable<TransitRepository>.queryRepos(transitType: TransitType): TransitRepository {
	return this.find { it.transitType == transitType } ?: throw IllegalStateException("Transit type must exist in the iterable")
}

/** @throws IllegalStateException When the list does not contain the repo of the transitType. */
@Throws(IllegalStateException::class)
fun Iterable<FavouritesRepository>.queryRepos(transitType: TransitType): FavouritesRepository {
	return this.find { it.transitType == transitType } ?: throw IllegalStateException("Transit type must exist in the iterable")
}

fun Context.toast(text: String) {
	Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(text: String) {
	Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(text: String) {
	Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
}

/**
 * Find the cause/exception that threw this exception with the same type as given to the type parameter.
 * @return null if the type of exception searched for wasn't a cause of this exception,
 * otherwise, returns the exception.
 */
inline fun <reified T : Throwable> Throwable.findCause(): T? {
	var current: Throwable? = this
	while (current != null) {
		if (current is T) return current
		current = current.cause
	}
	return null
}
