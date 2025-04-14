package dev.mainhq.bus2go.presentation.choose_direction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetDirections
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ChooseDirectionViewModel(
	routeInfo: RouteInfo,
	private val getDirections: GetDirections,
	private val getStopNames: GetStopNames
): ViewModel() {

	private val _leftDirection: MutableStateFlow<List<TransitData>?> = MutableStateFlow(null)
	val leftDirection = _leftDirection.asStateFlow()

	private val _rightDirection: MutableStateFlow<List<TransitData>?> = MutableStateFlow(null)
	val rightDirection = _rightDirection.asStateFlow()

	//if becomes true, let the user know
	private val _isUnidirectional: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isUnidirectional = _isUnidirectional.asStateFlow()

	init {
		viewModelScope.launch {

			//FIXME stopNames is nullable
			val stopNames = getStopNames.invoke(routeInfo)
			if (stopNames == null){
				println("No stop names found")
				return@launch
			}

			//FIXME instead of creating 1 item with empty stotpName (we are using it wrong),
			//create a list of ExoBusItem s
			when(routeInfo){
				is ExoBusRouteInfo -> {
					val headsigns = getDirections(routeInfo)
					_leftDirection.value = stopNames.first.map {
						ExoBusItem(
							routeId = routeInfo.routeId,
							stopName = it,
							direction = stopNames.first.last(),
							routeLongName = routeInfo.routeName,
							headsign = headsigns[0] as String
						)
					}

					if (headsigns.size > 1){
						_rightDirection.value = stopNames.second.map {
							ExoBusItem(
								routeId = routeInfo.routeId,
								stopName = it,
								direction = stopNames.second.last(),
								routeLongName = routeInfo.routeName,
								headsign = headsigns[1] as String
							)
						}
						_isUnidirectional.value = false
					}
					else {
						_isUnidirectional.value = true
					}

				}
				is ExoTrainRouteInfo -> {
					_leftDirection.value = stopNames.first.map {
						ExoTrainItem(
							routeId = routeInfo.routeId,
							//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
							stopName = it,
							direction = stopNames.first.last(),
							trainNum = routeInfo.trainNum,
							routeName = routeInfo.routeName,
							directionId = 0,
						)
					}

					_rightDirection.value = stopNames.second.map {
						ExoTrainItem(
							routeId = routeInfo.routeId,
							//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
							stopName = it,
							direction = stopNames.second.last(),
							trainNum = routeInfo.trainNum,
							routeName = routeInfo.routeName,
							directionId = 1,
						)
					}
					_isUnidirectional.value = false
				}
				is StmBusRouteInfo -> {
					val directions = getDirections(routeInfo) as List<DirectionInfo>
					_leftDirection.value = stopNames.first.map {
						StmBusItem(
							routeId = routeInfo.routeId,
							stopName = it,
							direction = directions[0].tripHeadSign,
							directionId = directions[0].directionId,
							lastStop = stopNames.first.last()
						)
					}

					if (directions.size > 1){
						_rightDirection.value = stopNames.second.map {
							StmBusItem(
								routeId = routeInfo.routeId,
								stopName = it,
								direction = directions[1].tripHeadSign,
								directionId = directions[1].directionId,
								lastStop = stopNames.second.last()
							)
						}
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