package dev.mainhq.bus2go.presentation.choose_direction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.core.Result
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

	//TODO use UiState sealed class insted of nullable stateFlows to stay consistent

	private val _leftDirection: MutableStateFlow<List<TransitData>?> = MutableStateFlow(null)
	val leftDirection = _leftDirection.asStateFlow()

	private val _rightDirection: MutableStateFlow<List<TransitData>?> = MutableStateFlow(null)
	val rightDirection = _rightDirection.asStateFlow()

	//FIXME could use inside of a class with the other data instead...
	private val _textColour: MutableStateFlow<Int?> = MutableStateFlow(null)
	val textColour = _textColour.asStateFlow()

	//if becomes true, let the user know
	private val _isUnidirectional: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isUnidirectional = _isUnidirectional.asStateFlow()

	init {
		viewModelScope.launch {
			//FIXME instead of creating 1 item with empty stopName (we are using it wrong),
			// create a list of ExoBusItems
			when(val stopNames = getStopNames.invoke(routeInfo)){
				is Result.Error -> {
					//TODO print a message in the ui (either dialog, or snackbar to incite download
					// and then abort the rest of the function...
				}

				is Result.Success<Pair<List<String>, List<String>>> -> {
					when(routeInfo){
						is ExoBusRouteInfo -> {
							_textColour.value = R.color.basic_purple
							when(val headsigns = getDirections.invoke(routeInfo)){
								is Result.Error -> {
									TODO()
								}

								is Result.Success<List<DirectionInfo>> -> {
									_leftDirection.value = stopNames.data.first.map {
										ExoBusItem(
											routeId = routeInfo.routeId,
											stopName = it,
											direction = headsigns.data.first().tripHeadSign,
											routeLongName = routeInfo.routeName,
										)
									}

									if (headsigns.data.size > 1){
										_rightDirection.value = stopNames.data.second.map {
											ExoBusItem(
												routeId = routeInfo.routeId,
												stopName = it,
												direction = headsigns.data.last().tripHeadSign,
												routeLongName = routeInfo.routeName,
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

						is ExoTrainRouteInfo -> {
							_textColour.value = R.color.orange
							_leftDirection.value = stopNames.data.first.map {
								ExoTrainItem(
									routeId = routeInfo.routeId,
									//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
									stopName = it,
									direction = stopNames.data.first.last(),
									trainNum = routeInfo.trainNum,
									routeName = routeInfo.routeName,
									directionId = 0,
								)
							}

							_rightDirection.value = stopNames.data.second.map {
								ExoTrainItem(
									routeId = routeInfo.routeId,
									//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
									stopName = it,
									direction = stopNames.data.second.last(),
									trainNum = routeInfo.trainNum,
									routeName = routeInfo.routeName,
									directionId = 1,
								)
							}
							_isUnidirectional.value = false
						}

						is StmBusRouteInfo -> {
							_textColour.value = R.color.basic_blue
							when(val directions = getDirections.invoke(routeInfo)){
								is Result.Error -> {
									TODO()
								}

								is Result.Success<List<DirectionInfo>> -> {
									//FIXME this is a mini hack...
									directions.data as List<DirectionInfo.StmDirectionInfo>
									_leftDirection.value = stopNames.data.first.map {
										StmBusItem(
											routeId = routeInfo.routeId,
											stopName = it,
											direction = directions.data[0].tripHeadSign,
											directionId = directions.data[0].directionId,
											lastStop = stopNames.data.first.last()
										)
									}

									if (directions.data.size > 1){
										_rightDirection.value = stopNames.data.second.map {
											StmBusItem(
												routeId = routeInfo.routeId,
												stopName = it,
												direction = directions.data[1].tripHeadSign,
												directionId = directions.data[1].directionId,
												lastStop = stopNames.data.second.last()
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
			}

		}
	}
}