package dev.mainhq.bus2go.presentation.stop_direction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StopDirectionViewModel: ViewModel() {

	private val _animationDirection: MutableStateFlow<AnimationDirection?> = MutableStateFlow(null)
	val animationDirection = _animationDirection.asStateFlow()
	private val _previousAnimationDirection: MutableStateFlow<AnimationDirection?> = MutableStateFlow(null)
	val previousAnimationDirection = _previousAnimationDirection.asStateFlow()

	fun setAnimationDirection(animationDirection: AnimationDirection){
		if (animationDirection == AnimationDirection.FROM_TIMES_ACTIVITY){
			_previousAnimationDirection.update { _animationDirection.value }
		}
		_animationDirection.update { animationDirection }
	}

	fun toTimesActivity(){
		_previousAnimationDirection.update { _animationDirection.value }
		_animationDirection.update { AnimationDirection.FROM_TIMES_ACTIVITY }
	}

	fun resetCache() {
		_previousAnimationDirection.update { null }
	}
}