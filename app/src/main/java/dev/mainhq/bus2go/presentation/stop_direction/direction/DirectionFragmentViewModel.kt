package dev.mainhq.bus2go.presentation.stop_direction.direction

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
import dev.mainhq.bus2go.presentation.core.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//FIXME we are using the same class as the one used for favourites, meaning that we are storing an
// empty list of tags for nothing... eventually change this shit

class DirectionFragmentViewModel(
	private val getDirections: GetDirections,
	private val getStopNames: GetStopNames,
): ViewModel() {

	private val _routeInfo = MutableStateFlow<UiState<RouteInfo>>(UiState.Loading)
	val routeInfo = _routeInfo.asStateFlow()

	//TODO use UiState sealed class insted of nullable stateFlows to stay consistent
	private val _topDirection: MutableStateFlow<List<TransitData>?> = MutableStateFlow(null)
	val topDirection = _topDirection.asStateFlow()

	private val _bottomDirection: MutableStateFlow<List<TransitData>?> = MutableStateFlow(null)
	val bottomDirection = _bottomDirection.asStateFlow()

	//FIXME could use inside of a class with the other data instead...
	private val _textColour: MutableStateFlow<Int?> = MutableStateFlow(null)
	val textColour = _textColour.asStateFlow()

	//FIXME could use inside of a class with the other data instead...
	private val _cardViewColour: MutableStateFlow<Int?> = MutableStateFlow(null)
	val cardViewColour = _cardViewColour.asStateFlow()

	//if becomes true, let the user know
	private val _isUnidirectional: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isUnidirectional = _isUnidirectional.asStateFlow()

	fun setRouteInfo(routeInfo: RouteInfo) {
		viewModelScope.launch {
			//FIXME instead of creating 1 item with empty stopName (we are using it wrong),
			// create a list of ExoBusItems
			when(val stopNames = getStopNames.invoke(routeInfo)){
				is Result.Error -> {
					//TODO print a message in the ui (either dialog, or snackbar to incite download
					// and then abort the rest of the function...
				}

				is Result.Success<Pair<List<String>, List<String>>> -> {
					_routeInfo.value = UiState.Success(routeInfo)
					when(routeInfo){
						is ExoBusRouteInfo -> {
							_textColour.value = R.color.basic_purple
							_cardViewColour.value = R.color.basic_purple
							//_cardViewColour.value = R.color.transparent_basic_purple
							when(val headsigns = getDirections.invoke(routeInfo)){
								is Result.Error -> {
									TODO()
								}

								is Result.Success<List<DirectionInfo>> -> {
									_topDirection.update {
										stopNames.data.first.map {
											//FIXME normally, we should use a separate class, but for
											// simplicity, it works for now
											ExoBusItem(
												routeId = routeInfo.routeId,
												stopName = it,
												direction = headsigns.data.first().tripHeadSign,
												routeLongName = routeInfo.routeName,
												tags = listOf()
											)
										}
									}

									if (headsigns.data.size > 1){
										_bottomDirection.update {
											stopNames.data.second.map {
												ExoBusItem(
													routeId = routeInfo.routeId,
													stopName = it,
													direction = headsigns.data.last().tripHeadSign,
													routeLongName = routeInfo.routeName,
													tags = listOf()
												)
											}
										}
										_isUnidirectional.update { false }
									}
									else {
										_isUnidirectional.update { true }
									}
								}
							}
						}

						is ExoTrainRouteInfo -> {
							_textColour.value = R.color.orange
							_cardViewColour.value = R.color.orange
							//_cardViewColour.value = R.color.transparent_orange
							_topDirection.value = stopNames.data.first.map {
								ExoTrainItem(
									routeId = routeInfo.routeId,
									//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
									stopName = it,
									direction = stopNames.data.first.last(),
									trainNum = routeInfo.trainNum,
									routeName = routeInfo.routeName,
									directionId = 0,
									tags = listOf()
								)
							}

							_bottomDirection.value = stopNames.data.second.map {
								ExoTrainItem(
									routeId = routeInfo.routeId,
									//DO NOT SET IT YET SINCE WE ARE ONLY CHOOSING A DIR
									stopName = it,
									direction = stopNames.data.second.last(),
									trainNum = routeInfo.trainNum,
									routeName = routeInfo.routeName,
									directionId = 1,
									tags = listOf()
								)
							}
							_isUnidirectional.value = false
						}

						is StmBusRouteInfo -> {
							_textColour.value = R.color.basic_blue
							_cardViewColour.value = R.color.basic_blue
							//_cardViewColour.value = R.color.transparent_basic_blue
							when(val directions = getDirections.invoke(routeInfo)){
								is Result.Error -> {
									TODO()
								}

								is Result.Success<List<DirectionInfo>> -> {
									//FIXME this is a mini hack...
									directions.data as List<DirectionInfo.StmDirectionInfo>
									_topDirection.value = stopNames.data.first.map {
										StmBusItem(
											routeId = routeInfo.routeId,
											stopName = it,
											direction = directions.data[0].tripHeadSign,
											directionId = directions.data[0].directionId,
											lastStop = stopNames.data.first.last(),
											tags = listOf()
										)
									}

									if (directions.data.size > 1){
										_bottomDirection.value = stopNames.data.second.map {
											StmBusItem(
												routeId = routeInfo.routeId,
												stopName = it,
												direction = directions.data[1].tripHeadSign,
												directionId = directions.data[1].directionId,
												lastStop = stopNames.data.second.last(),
												tags = listOf()
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