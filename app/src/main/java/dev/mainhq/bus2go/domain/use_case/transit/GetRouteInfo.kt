package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.exceptions.DatabaseFormatingException
import dev.mainhq.bus2go.domain.repository.TransitRepository
import dev.mainhq.bus2go.utils.queryRepos

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