package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.ExoBusData
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.ExoTrainData
import dev.mainhq.bus2go.data.repository.FakeExoFavouritesRepo
import dev.mainhq.bus2go.data.repository.FakeStmFavouritesRepo
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AddFavouriteTest {

	private lateinit var fakeExoFavouritesRepo: FakeExoFavouritesRepo
	private lateinit var fakeStmFavouritesRepo: FakeStmFavouritesRepo
	private lateinit var addFavourite: AddFavourite

	@Before
	fun setup(){
		fakeExoFavouritesRepo = FakeExoFavouritesRepo()
		fakeStmFavouritesRepo = FakeStmFavouritesRepo()
		addFavourite = AddFavourite(fakeExoFavouritesRepo, fakeStmFavouritesRepo)
	}

	@Test
	fun `Add Stm Favourite to non-empty`(){
		runBlocking {
			val data = StmBusItem("foo", "bar", "newDir", 10, "last")
			val size = fakeStmFavouritesRepo.stmItems.size
			addFavourite(data)
			assert(fakeStmFavouritesRepo.stmItems.size == size + 1)
		}
	}

	@Test
	fun `Add Exo Bus Favourite to non-empty`(){
		runBlocking {
			val data = ExoBusItem("foo", "bar", "newDir", "longName", "last")
			val size = fakeExoFavouritesRepo.exoBusItems.size
			addFavourite(data)
			assert(fakeExoFavouritesRepo.exoBusItems.size == size + 1)
		}
	}

	@Test
	fun `Add Exo Train Favourite to non-empty`(){
		runBlocking {
			val data = ExoTrainItem("foo", "bar", "newDir", 10, "last", 10)
			val size = fakeExoFavouritesRepo.exoTrainItems.size
			addFavourite(data)
			assert(fakeExoFavouritesRepo.exoTrainItems.size == size + 1)
		}
	}

}