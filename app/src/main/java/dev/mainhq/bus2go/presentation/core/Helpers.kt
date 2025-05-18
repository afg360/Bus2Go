package dev.mainhq.bus2go.presentation.core

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

/** Starts a new coroutine on the Main thread collecting data from the viewModel*/
fun <T> Fragment.collectFlow(flow: Flow<T>, flowCollector: FlowCollector<T>){
	viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main){
		viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
			flow.collect(flowCollector)
		}
	}
}