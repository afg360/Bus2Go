package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.domain.exceptions.DatabaseFormatingException
import dev.mainhq.bus2go.domain.exceptions.DirectionsMissingException
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmRepository

/**
 * Used to get the stopNames of a routeInfo.
 * Also used to get the directions.
 **/
class GetStopNames(
	private val logger: Logger,
	private val exoRepository: ExoRepository,
	private val stmRepository: StmRepository
) {

	/**
	 * @return A pair of stopNames as strings. May return a pair of empty list or a pair of one non-empty
	 * list.
	 * @throws DatabaseFormatingException When the database contains STM routeId which is not a number.
	 * @throws DirectionsMissingException When the database cannot have directions for an ExoBus.
	 **/
	suspend operator fun invoke(routeInfo: RouteInfo): Pair<List<String>, List<String>> {
		when(routeInfo){
			is ExoBusRouteInfo -> {
				val directions = exoRepository.getBusTripHeadsigns(routeInfo.routeId)
				return if (directions.isEmpty()) throw DirectionsMissingException("The route $routeInfo doesn't have any directions to it...")
				else if (directions.size == 1) exoRepository.getStopNames(directions.first(), null)
				else exoRepository.getStopNames(
					directions.first(),
					directions.last()
				)
			}
			is ExoTrainRouteInfo -> return exoRepository.getTrainStopNames(routeInfo.routeId)
			is StmBusRouteInfo -> {
				//busNum <= 5 are skipped, bcz these are metros
				try {
					if (routeInfo.routeId.toInt() > 5) {
						val directions = stmRepository.getDirectionInfo(routeInfo.routeId.toInt())
						return stmRepository.getStopNames(
							directions.first().tripHeadSign,
							directions.last().tripHeadSign,
							routeInfo.routeId
						)
					}
					else {
						logger.warn("STM_BUS_NUM", "A metro bus num was given... not dealing with this shit for now...")
						return Pair(listOf(), listOf())
					}
				}
				catch (nfm: NumberFormatException){
					logger.error("STM_ROUTE_ID", "Expected an integer in the db but received a string", null)
					throw DatabaseFormatingException("Expected an integer in the db but received a string")
				}
			}
		}

	}
}