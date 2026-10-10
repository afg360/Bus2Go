package app.bus2go.android.domain.use_case.transit

import app.bus2go.android.domain.core.Result
import app.bus2go.android.domain.entity.ExoBusRouteInfo
import app.bus2go.android.domain.entity.ExoTrainRouteInfo
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.domain.entity.StmBusRouteInfo
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.entity.stm.DirectionInfo
import app.bus2go.android.domain.exceptions.DatabaseFormatingException
import app.bus2go.android.domain.exceptions.DirectionsMissingException
import app.bus2go.android.domain.repository.TransitRepository
import app.bus2go.android.utils.queryRepos

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
						if (directions.data.isEmpty()) {
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
				catch (nfm: NumberFormatException){
					return Result.Error(DatabaseFormatingException("Expected an integer in the db but received a string"))
				}
			}
		}

	}
}