package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.domain.entity.FuzzyQuery
import dev.mainhq.bus2go.domain.exceptions.DatabaseFormatingException

/**
 * Class for querying transit data. Performs a search using FuzzyQuery.
 **/
class GetRouteInfo (
	private val exoRepository: ExoRepository,
	private val stmRepository: StmRepository,
){

	suspend operator fun invoke(query: String): Result<List<RouteInfo>>{
		val stmRouteInfoResult = stmRepository.getBusRouteInfo(FuzzyQuery(query))
		val exoRouteInfo = exoRepository.getRouteInfo(FuzzyQuery(query, true))
		if (stmRouteInfoResult is Result.Error && exoRouteInfo is Result.Error)
			//FIXME could be a better exception...
			return Result.Error(DatabaseFormatingException(), "Database does not exist...")
		val list = mutableListOf<RouteInfo>()
		if (stmRouteInfoResult is Result.Success) list.addAll(stmRouteInfoResult.data)
		if (exoRouteInfo is Result.Success) list.addAll(exoRouteInfo.data)

		return Result.Success(list)
	}
}