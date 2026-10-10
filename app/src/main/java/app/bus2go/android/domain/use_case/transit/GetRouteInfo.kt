package app.bus2go.android.domain.use_case.transit

import app.bus2go.android.domain.core.Result
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.domain.entity.FuzzyQuery
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.exceptions.DatabaseFormatingException
import app.bus2go.android.domain.repository.TransitRepository
import app.bus2go.android.utils.queryRepos

/**
 * Class for querying transit data. Performs a search using FuzzyQuery.
 **/
class GetRouteInfo (
	private val transitRepos: List<TransitRepository>
){

	suspend operator fun invoke(query: String): Result<List<RouteInfo>>{
		val stmRouteInfoResult = transitRepos.queryRepos(TransitType.STM).getRouteInfo(FuzzyQuery(query))
		val exoRouteInfo = transitRepos.queryRepos(TransitType.EXO_BUS).getRouteInfo(FuzzyQuery(query, true))
		//TODO for exoTrains also...?

		if (stmRouteInfoResult is Result.Error && exoRouteInfo is Result.Error)
			//FIXME could be a better exception...
			return Result.Error(DatabaseFormatingException(), "Database does not exist...")
		val list = mutableListOf<RouteInfo>()
		if (stmRouteInfoResult is Result.Success) list.addAll(stmRouteInfoResult.data)
		if (exoRouteInfo is Result.Success) list.addAll(exoRouteInfo.data)

		return Result.Success(list)
	}
}