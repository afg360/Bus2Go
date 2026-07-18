package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.entity.stm.DirectionInfo
import dev.mainhq.bus2go.domain.exceptions.DatabaseFormatingException
import dev.mainhq.bus2go.domain.exceptions.DirectionsMissingException
import dev.mainhq.bus2go.domain.repository.TransitRepository
import dev.mainhq.bus2go.utils.queryRepos

/**
 * Used to get the stopNames of a routeInfo.
 * Also used to get the directions.
 **/
class GetStopNames(
	private val transitRepos: List<TransitRepository>,
) {

	/**
	 * @return A pair of stopNames as strings. May return a pair of empty list or a pair of one non-empty
	 * list.
	 * @throws DatabaseFormatingException When the database contains STM routeId which is not a number.
	 * @throws DirectionsMissingException When the database cannot have directions for an ExoBus.
	 **/
	suspend operator fun invoke(routeInfo: RouteInfo): Result<Pair<List<String>, List<String>>> {
		when(routeInfo){
			is ExoBusRouteInfo -> {
				val repo = transitRepos.queryRepos(TransitType.EXO_BUS)

				return when(val directions = repo.getTripHeadsigns(routeInfo.routeId)){
					is Result.Error -> directions
					is Result.Success<List<DirectionInfo>> -> {
						return if (directions.data.isEmpty()) {
							Result.Error(DirectionsMissingException("The route $routeInfo doesn't have any directions to it..."))
						}
						else if (directions.data.size == 1) {
							repo.getStopNames(directions.data.first().tripHeadSign, null, null)
						}
						else {
							repo.getStopNames(
								directions.data.first().tripHeadSign,
								directions.data.last().tripHeadSign,
								null
							)
						}
					}
				}
			}
			is ExoTrainRouteInfo -> {
				return transitRepos.queryRepos(TransitType.EXO_TRAIN).getStopNames(
					routeInfo.routeId,
					null,
					null
				)
			}
			is StmBusRouteInfo -> {
				//busNum <= 5 are skipped, bcz these are metros
				try {
					if (routeInfo.routeId.toInt() > 5) {
						val repo = transitRepos.queryRepos(TransitType.STM)
						return when(val directions = repo.getTripHeadsigns(routeInfo.routeId)){
							is Result.Success -> {
								repo.getStopNames(
									directions.data.first().tripHeadSign,
									directions.data.last().tripHeadSign,
									routeInfo.routeId
								)
							}
							is Result.Error -> directions
						}
					}
					else {
						return Result.Success(Pair(listOf(), listOf()))
					}
				}
				catch (nfm: NumberFormatException){
					return Result.Error(DatabaseFormatingException("Expected an integer in the db but received a string"))
				}
			}
		}

	}
}