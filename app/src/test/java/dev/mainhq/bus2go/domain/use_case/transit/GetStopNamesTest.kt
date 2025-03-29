package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.data.core.TestLogger
import dev.mainhq.bus2go.data.repository.FakeExoRepository
import dev.mainhq.bus2go.data.repository.FakeStmRepository
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.exceptions.DatabaseFormatingException
import dev.mainhq.bus2go.domain.exceptions.DirectionsMissingException
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetStopNamesTest {

	private lateinit var getStopNames: GetStopNames
	private lateinit var fakeExoRepository: FakeExoRepository
	private lateinit var fakeStmRepository: FakeStmRepository
	private lateinit var logger: Logger

	@Before
	fun setup(){
		fakeExoRepository = FakeExoRepository()
		fakeStmRepository = FakeStmRepository()
		logger = TestLogger()
		getStopNames = GetStopNames(logger, fakeExoRepository, fakeStmRepository)

	}

	@Test
	fun `Get stop names for stm metro, expect Pair of empty`(){
		runBlocking {
			val metroRouteInfo =  fakeStmRepository.stmRouteInfo.find { it.routeId.toInt() <= 5 }!!
			val stopNames = getStopNames(metroRouteInfo)
			assert(stopNames.first.isEmpty() && stopNames.second.isEmpty())
		}
	}

	@Test(expected = DatabaseFormatingException::class)
	fun `Get stop names for bad formatted routeId, expect DatabaseFormatingException`(){
		runBlocking {
			val metroRouteInfo =  fakeStmRepository.stmRouteInfo.find { it.routeId.any{ !it.isDigit() } }!!
			val stopNames = getStopNames(metroRouteInfo)
		}
	}

	@Test
	fun `Get stopNames for stm metro, expect stopNames`(){
		runBlocking {
			logger.info("DEBUG", fakeStmRepository.stmRouteInfo.toString())
			val stmRouteInfo =  fakeStmRepository.stmRouteInfo.filter { it.routeId.all { it.isDigit() } }
				.filter { it.routeId.toInt() > 5 }
				.random()
			val stopNames = getStopNames.invoke(stmRouteInfo)
			val stmBusItems = fakeStmRepository.stmTransitData.filter { it.routeId == stmRouteInfo.routeId }
			val expected = Pair(
				stmBusItems.filter { it.directionId == 0 }.map { it.stopName },
				stmBusItems.filter { it.directionId == 1 }.map { it.stopName }
			)
			assert(stopNames == expected)
		}
	}

	@Test
	fun `Get stopNames for exo buses, expect stopNames`(){
		//TODO improve the test by comparing the actual data queried and the method used
		runBlocking {
			val routeInfo = fakeExoRepository.exoBusRouteInfo.random()
			val stopNames = getStopNames(routeInfo)
			assert(stopNames.first.isNotEmpty() && stopNames.second.isNotEmpty())
		}
	}

	@Test(expected = DirectionsMissingException::class)
	fun `Get stopNames for exo buses from non-existing routeInfo, expect DirectionMissingException`(){
		runBlocking {
			val routeInfo = ExoBusRouteInfo("random", "non-existing route")
			assert(getStopNames(routeInfo) == Pair<List<ExoBusItem>, List<ExoBusItem>>(listOf(), listOf()))
		}
	}

	@Test
	fun `Get stopNames for exo trains, expect stopNames`(){
		runBlocking {
			val stopNames = getStopNames(fakeExoRepository.exoTrainRouteInfo.random())
			assert(stopNames.first.isNotEmpty() && stopNames.first.isNotEmpty())
		}
	}

	@Test
	fun `Get stopNames for non-existing exo trains, expect empty lists`(){
		runBlocking {
			val routeInfo = ExoTrainRouteInfo("foo", "bar", 3)
			val stopNames = getStopNames(routeInfo)
			assert(stopNames.first.isEmpty() && stopNames.first.isEmpty())
		}
	}

}