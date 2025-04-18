package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.data.core.TestLogger
import dev.mainhq.bus2go.data.repository.FakeExoRepository
import dev.mainhq.bus2go.data.repository.FakeStmRepository
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetRouteInfoTest {

	private lateinit var getRouteInfo: GetRouteInfo
	private lateinit var fakeExoRepository: FakeExoRepository
	private lateinit var fakeStmRepository: FakeStmRepository
	private lateinit var logger: Logger

	@Before
	fun setup(){
		logger = TestLogger()
		fakeExoRepository = FakeExoRepository()
		fakeStmRepository = FakeStmRepository()
		getRouteInfo = GetRouteInfo(fakeExoRepository, fakeStmRepository)
	}

	@Test
	fun `Query random things, expect empty list`(){
		runBlocking {
			assert(getRouteInfo.invoke("random data that doesn't exist").isEmpty())
		}
	}

	@Test
	fun `Query a shared routeId portion, expect a list of many type`(){
		runBlocking {
			val res = getRouteInfo.invoke("1")

			//we have 2 stm buses, 3 exo buses, 1 train containing a 1
			assert(res.size == 6)
		}
	}

	@Test
	fun `Query STM only, expect list of Stm Route Info`(){
		runBlocking {
			val query = fakeStmRepository.stmRouteInfo.random().routeName
			assert(getRouteInfo.invoke(query).all { it::class.java == StmBusRouteInfo::class.java })
		}
	}

	@Test
	fun `Query Exo Bus only, expect list of Exo Route Info`(){
		runBlocking {
			val query = fakeExoRepository.exoBusRouteInfo.random().routeName
			assert(getRouteInfo.invoke(query).all { it::class.java == ExoBusRouteInfo::class.java })
		}
	}

	@Test
	fun `Query Exo train, expect list of Exo Route Info`(){
		runBlocking {
			val query = fakeExoRepository.exoTrainRouteInfo.random().routeName
			assert(getRouteInfo.invoke(query).all { it::class.java == ExoTrainRouteInfo::class.java })
		}
	}

}