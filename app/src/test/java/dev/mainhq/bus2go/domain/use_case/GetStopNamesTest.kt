package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.data.core.TestLogger
import dev.mainhq.bus2go.data.repository.FakeExoRepository
import dev.mainhq.bus2go.data.repository.FakeStmRepository
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
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
			assert(stopNames != null && (stopNames.first.isEmpty() && stopNames.second.isEmpty()))
		}
	}

	@Test
	fun `Get stop names for bad formatted routeId, expect null`(){
		runBlocking {
			val metroRouteInfo =  fakeStmRepository.stmRouteInfo.find { it.routeId.any{ !it.isDigit() } }!!
			val stopNames = getStopNames(metroRouteInfo)
			assert(stopNames == null)
		}
	}



	@Test
	fun `Get stop names for stm metro, expect stopNames`(){
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
			assert(stopNames != null && stopNames == expected)
		}
	}

	@Test
	fun testExoBus(){
		runBlocking {
			//getStopNames()
		}
	}

	@Test
	fun testExoTrain(){
		runBlocking {
			//getStopNames()
		}
	}
}