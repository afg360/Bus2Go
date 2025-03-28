package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmRepository
import dev.mainhq.bus2go.domain.entity.FuzzyQuery

/**
 * Class for querying transit data. Performs a search using FuzzyQuery.
 **/
class GetRouteInfo (
	private val exoRepository: ExoRepository,
	private val stmRepository: StmRepository,
){

	suspend operator fun invoke(query: String): List<RouteInfo>{
		return stmRepository.getBusRouteInfo(FuzzyQuery(query)) +
				exoRepository.getRouteInfo(FuzzyQuery(query, true))
	}
}