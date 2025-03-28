package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.data.repository.FakeExoRepository
import dev.mainhq.bus2go.data.repository.FakeStmRepository
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.lang.Character.isDigit

class GetStopNamesTest {

	private lateinit var getStopNames: GetStopNames
	private lateinit var fakeExoRepository: FakeExoRepository
	private lateinit var fakeStmRepository: FakeStmRepository

	@Before
	fun setup(){
		fakeExoRepository = FakeExoRepository()
		fakeStmRepository = FakeStmRepository()
		getStopNames = GetStopNames(fakeExoRepository, fakeStmRepository)

	}

	@Test
	fun `Get stop names for stm metro, expect null`(){
		runBlocking {
			val metroRouteInfo =  fakeStmRepository.stmRouteInfo.find { it.routeId.toInt() <= 5 }!!
			val stopNames = getStopNames(metroRouteInfo)
			assertThat(stopNames == null)
		}
	}

	@Test
	fun `Get stop names for stm metro, expect stopNames`(){
		runBlocking {
			println(fakeStmRepository.stmRouteInfo)
			val stmRouteInfo =  fakeStmRepository.stmRouteInfo.filter { it.routeId.all { it.isDigit() } }
				.filter { it.routeId.toInt() > 5 }
				.random()
			val stopNames = getStopNames(stmRouteInfo)
			val stmBusItems = fakeStmRepository.stmTransitData.filter { it.routeId == stmRouteInfo.routeId }
			val expected = Pair(
				stmBusItems.filter { it.directionId == 0 }.map { it.stopName },
				stmBusItems.filter { it.directionId == 1 }.map { it.stopName }
			)
			assertThat(stopNames != null && stopNames == expected)
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