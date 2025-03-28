package dev.mainhq.bus2go.domain.use_case.transit

import android.util.Log
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.domain.repository.ExoRepository
import dev.mainhq.bus2go.domain.repository.StmRepository

/**
 * Used to get the stopNames of a routeInfo.
 * Also used to get the directions.
 **/
class GetStopNames(
	private val exoRepository: ExoRepository,
	private val stmRepository: StmRepository
) {

	/**
	 * @return A pair of stopNames as strings. If an STM metro was given,
	 * returns null instead.
	 **/
	suspend operator fun invoke(routeInfo: RouteInfo): Pair<List<String>, List<String>>? {
		when(routeInfo){
			is ExoBusRouteInfo -> {
				val directions = exoRepository.getTripHeadsigns(routeInfo.routeId)
				return exoRepository.getStopNames(
					Pair(
						directions.first(),
						directions.last()
					)
				)
			}
			is ExoTrainRouteInfo -> return exoRepository.getTrainStopNames(routeInfo.routeId)
			is StmBusRouteInfo -> {
				//busNum <= 5 are skipped, bcz these are metros
				try {
					if (routeInfo.routeId.toInt() > 5) {
						val directions = stmRepository.getDirectionInfo(routeInfo.routeId.toInt())
						return stmRepository.getStopNames(
							Pair(
								directions.first().tripHeadSign,
								directions.last().tripHeadSign
							),
							routeInfo.routeId
						)
					} else {
						//Log.w(
						//	"STM_BUS_NUM",
						//	"A metro bus num was given... not dealing with this shit for now..."
						//)
						//return Pair(listOf(), listOf())
						return null
					}
				}
				catch (nfm: NumberFormatException){
					//Log.e("STM_ROUTE_ID", "Expected an integer in the db but received a string")
					return null
				}
			}
		}

	}
}