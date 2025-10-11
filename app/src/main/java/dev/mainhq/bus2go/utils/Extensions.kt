package dev.mainhq.bus2go.utils

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.mainhq.bus2go.presentation.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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


fun <T> AppCompatActivity.launchViewModelCollect(flow: Flow<T>, context: CoroutineContext = Dispatchers.Main, block: suspend (T) -> Unit){
	lifecycleScope.launch(context) {
		lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
			flow.collect(block)
		}
	}
}

fun <T> Fragment.launchViewModelCollect(flow: Flow<T>, context: CoroutineContext = Dispatchers.Main, block: suspend (T) -> Unit){
	viewLifecycleOwner.lifecycleScope.launch(context) {
		viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
			flow.collect(block)
		}
	}
}
