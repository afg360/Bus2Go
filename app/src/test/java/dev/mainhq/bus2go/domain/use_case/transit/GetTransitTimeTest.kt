package dev.mainhq.bus2go.domain.use_case.transit

import dev.mainhq.bus2go.data.core.TestLogger
import dev.mainhq.bus2go.data.repository.FakeExoRepository
import dev.mainhq.bus2go.data.repository.FakeStmRepository
import dev.mainhq.bus2go.domain.core.Logger
import dev.mainhq.bus2go.domain.core.Result
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.Time
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

class GetTransitTimeTest {

	private lateinit var logger: Logger
	private lateinit var fakeExoRepository: FakeExoRepository
	private lateinit var fakeStmRepository: FakeStmRepository
	private lateinit var getTransitTime: GetTransitTime
	private lateinit var curTime: Time

	//TODO should prob also test for curTime when it has passed some time...

	@Before
	fun setup(){
		logger = TestLogger()
		fakeExoRepository = FakeExoRepository()
		fakeStmRepository = FakeStmRepository()
		getTransitTime = GetTransitTime(fakeExoRepository, fakeStmRepository)
		curTime = Time(fakeExoRepository.testDate, LocalTime.now())
	}

	@Test
	fun `Get Exo Bus Transit Time from non-existing bus, expect empty list`(){
		runBlocking {
			val transitdata = ExoBusItem("foo", "bar", "non-existing", "longName", "random")
			val info = getTransitTime.invoke(curTime, transitdata) as Result.Success<List<Time>>
			assert(info.data.isEmpty())
		}
	}

	@Test
	fun `Get Exo Train Transit Time from non-existing train, expect empty list`(){
		runBlocking {
			val transitdata = ExoTrainItem("foo", "bar", "non-existing", 99, "random", 10)
			val info = getTransitTime.invoke(curTime, transitdata) as Result.Success<List<Time>>
			assert(info.data.isEmpty())
		}
	}

	@Test
	fun `Get Stm Bus Transit Time from non-existing bus, expect empty list`(){
		runBlocking {
			val transitdata = StmBusItem("foo", "bar", "non-existing", 10, "random")
			val info = getTransitTime.invoke(curTime, transitdata) as Result.Success<List<Time>>
			assert(info.data.isEmpty())
		}
	}

	@Test
	fun `Get Exo Bus Transit Time, expect some non-empty list`(){
		runBlocking {
			val transitdata = fakeExoRepository.exoBusTransitData.random()
			val time = fakeExoRepository.stopTimesBus[transitdata]!!
			val info = getTransitTime.invoke(curTime, transitdata) as Result.Success<List<Time>>
			assert(info.data.size == time.size)
		}
	}

	@Test
	fun `Get Exo train Transit Time, expect some non-empty list`(){
		runBlocking {
			val transitdata = fakeExoRepository.exoTrainTransitData.random()
			val time = fakeExoRepository.stopTimesTrain[transitdata]!!
			val info = getTransitTime.invoke(curTime, transitdata) as Result.Success<List<Time>>
			assert(info.data.size == time.size)
		}
	}

	@Test
	fun `Get Stm Bus Transit Time, expect some non-empty list`(){
		runBlocking {
			val transitdata = fakeStmRepository.stmTransitData.random()
			val time = fakeStmRepository.stopTimes[transitdata]!!
			val info = getTransitTime.invoke(curTime, transitdata) as Result.Success<List<Time>>
			assert(info.data.size == time.size)
		}
	}

}