package dev.mainhq.bus2go.utils

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.CoroutineContext


/** Assumes that the long in question is in milliseconds */
fun Long.toEpochDay(): Long{
	return this / (3600 * 1000 * 24)
}

fun LocalDate.toEpochMillis(): Long {
	return this.toEpochDay() * 24 * 3600 * 1000
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

/**
 * Find the cause/exception that threw this exception with the same type as given to the type parameter
 * @return null if the type of exception searched for wasn't a cause of this exception
 * Otherwise, returns the exception
 */
inline fun <reified T : Throwable> Throwable.findCause(): T? {
	var current: Throwable? = this
	while (current != null) {
		if (current is T) return current
		current = current.cause
	}
	return null
}
