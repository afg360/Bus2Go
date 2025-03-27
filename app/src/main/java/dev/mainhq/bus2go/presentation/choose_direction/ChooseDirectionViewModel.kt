package dev.mainhq.bus2go.presentation.choose_direction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoBusTransitDataWithStopNames
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainTransitDataWithStopNames
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitDataWithStopNames
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusTransitDataWithStopNames
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetDirections
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ChooseDirectionViewModel(
	private val routeInfo: RouteInfo,
	private val getDirections: GetDirections,
	private val getStopNames: GetStopNames
): ViewModel() {

	private val _leftDirection: MutableStateFlow<TransitDataWithStopNames?> = MutableStateFlow(null)
	val leftDirection = _leftDirection.asStateFlow()

	private val _rightDirection: MutableStateFlow<TransitDataWithStopNames?> = MutableStateFlow(null)
	val rightDirection = _rightDirection.asStateFlow()

	//if becomes true, let the user know
	private val _isUnidirectional: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isUnidirectional = _isUnidirectional.asStateFlow()

	init {
		viewModelScope.launch {

			//FIXME stopNames is nullable
			val stopNames = getStopNames(routeInfo)
			if (stopNames == null){
				println("No stop names found")
				return@launch
			}

			//TODO bad thing with StopName fields... SET IT TO EMPTY STRING SINCE WE ARE ONLY CHOOSING A DIR
			when(routeInfo){
				is ExoBusRouteInfo -> {
					val headsigns = getDirections(routeInfo)
					_leftDirection.value = ExoBusTransitDataWithStopNames(
						ExoBusItem(
							routeId = routeInfo.routeId,
							stopName = "",
							direction = stopNames.first.last(),
							routeLongName = routeInfo.routeName,
							headsign = headsigns[0] as String
						),
						stopNames.first
					)

					if (headsigns.size == 1){
						_rightDirection.value = ExoBusTransitDataWithStopNames(
							ExoBusItem(
								routeId = routeInfo.routeId,
								//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
								stopName = "",
								direction = stopNames.second.last(),
								routeLongName = routeInfo.routeName,
								headsign = headsigns[1] as String
							),
							stopNames.second
						)
						_isUnidirectional.value = false
					}
					else {
						_isUnidirectional.value = true
					}

				}
				is ExoTrainRouteInfo -> {
					_leftDirection.value = ExoTrainTransitDataWithStopNames(
						ExoTrainItem(
							routeId = routeInfo.routeId,
							//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
							stopName = "",
							direction = stopNames.first.last(),
							trainNum = routeInfo.trainNum,
							routeName = routeInfo.routeName,
							directionId = 0,
						),
						stopNames.first
					)

					_rightDirection.value = ExoTrainTransitDataWithStopNames(
						ExoTrainItem(
							routeId = routeInfo.routeId,
							//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
							stopName = "",
							direction = stopNames.second.last(),
							trainNum = routeInfo.trainNum,
							routeName = routeInfo.routeName,
							directionId = 1,
						),
						stopNames.second
					)
					_isUnidirectional.value = false
				}
				is StmBusRouteInfo -> {
					val directions = getDirections(routeInfo) as List<DirectionInfo>
					_leftDirection.value = StmBusTransitDataWithStopNames(
						StmBusItem(
							routeId = routeInfo.routeId,
							stopName = "",
							direction = directions[0].tripHeadSign,
							directionId = directions[0].directionId,
							lastStop = stopNames.first.last()
						),
						stopNames.first
					)

					if (directions.size > 1){
						_rightDirection.value = StmBusTransitDataWithStopNames(
							StmBusItem(
								routeId = routeInfo.routeId,
								stopName = "",
								direction = directions[1].tripHeadSign,
								directionId = directions[1].directionId,
								lastStop = stopNames.second.last()
							),
							stopNames.second
						)
						_isUnidirectional.value = false
					}
					else {
						_isUnidirectional.value = true
					}
				}
			}
		}
	}

}