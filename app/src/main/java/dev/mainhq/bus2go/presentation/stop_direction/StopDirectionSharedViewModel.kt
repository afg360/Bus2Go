package dev.mainhq.bus2go.presentation.stop_direction

import androidx.lifecycle.ViewModel
import dev.mainhq.bus2go.domain.entity.TransitData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StopDirectionSharedViewModel: ViewModel() {

	//TODO what if only 1 direction present?
	private val _transitData: MutableStateFlow<List<TransitData>?> = MutableStateFlow(null)
	val transitDataUp = _transitData.asStateFlow()

	fun setupTransitData(transitData: List<TransitData>) {
		_transitData.update { transitData }
	}

}