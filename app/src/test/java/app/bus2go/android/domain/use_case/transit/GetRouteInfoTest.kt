package app.bus2go.android.domain.use_case.transit

import app.bus2go.android.data.core.TestLogger
import app.bus2go.android.domain.core.Logger
import org.junit.Before

class GetRouteInfoTest {

	private lateinit var getRouteInfo: GetRouteInfo
//	private lateinit var fakeExoRepository: FakeExoRepository
//	private lateinit var fakeStmRepository: FakeStmRepository
	private lateinit var logger: Logger

	@Before
	fun setup(){
		logger = TestLogger()
//		fakeExoRepository = FakeExoRepository()
//		fakeStmRepository = FakeStmRepository()
//		getRouteInfo = GetRouteInfo(fakeExoRepository, fakeStmRepository)
	}

//	@Test
//	fun `Query random things, expect empty list`(){
//		runBlocking {
//			val info = getRouteInfo.invoke("random data that doesn't exist") as Result.Success<List<RouteInfo>>
//			assert(info.data.isEmpty())
//		}
//	}
//
//	@Test
//	fun `Query a shared routeId portion, expect a list of many type`(){
//		runBlocking {
//			val res = getRouteInfo.invoke("1") as Result.Success<List<RouteInfo>>
//
//			//we have 2 stm buses, 3 exo buses, 1 train containing a 1
//			assert(res.data.size == 6)
//		}
//	}
//
//	@Test
//	fun `Query STM only, expect list of Stm Route Info`(){
//		runBlocking {
//			val query = fakeStmRepository.stmRouteInfo.random().routeName
//			val info = getRouteInfo.invoke(query) as Result.Success<List<RouteInfo>>
//			assert(info.data.all { it::class.java == StmBusRouteInfo::class.java })
//		}
//	}
//
//	@Test
//	fun `Query Exo Bus only, expect list of Exo Route Info`(){
//		runBlocking {
//			val query = fakeExoRepository.exoBusRouteInfo.random().routeName
//			val info = getRouteInfo.invoke(query) as Result.Success<List<RouteInfo>>
//			assert(info.data.all { it::class.java == ExoBusRouteInfo::class.java })
//		}
//	}
//
//	@Test
//	fun `Query Exo train, expect list of Exo Route Info`(){
//		runBlocking {
//			val query = fakeExoRepository.exoTrainRouteInfo.random().routeName
//			val info = getRouteInfo.invoke(query) as Result.Success<List<RouteInfo>>
//			assert(info.data.all { it::class.java == ExoTrainRouteInfo::class.java })
//		}
//	}
//
}