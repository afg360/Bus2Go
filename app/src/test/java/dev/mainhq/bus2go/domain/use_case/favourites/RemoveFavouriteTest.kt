package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.data.repository.FakeExoFavouritesRepo
import dev.mainhq.bus2go.data.repository.FakeStmFavouritesRepo
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class RemoveFavouriteTest {

	private lateinit var fakeExoFavouritesRepo: FakeExoFavouritesRepo
	private lateinit var fakeStmFavouritesRepo: FakeStmFavouritesRepo
	private lateinit var removeFavourite: RemoveFavourite

	@Before
	fun setup(){
		fakeExoFavouritesRepo = FakeExoFavouritesRepo()
		fakeStmFavouritesRepo = FakeStmFavouritesRepo()
		removeFavourite = RemoveFavourite(fakeExoFavouritesRepo, fakeStmFavouritesRepo)
	}

	@Test
	fun `Remove Stm Favourite to non-empty, expect size - 1`(){
		runBlocking {
			val data = fakeStmFavouritesRepo.stmItems.random()
			val size = fakeStmFavouritesRepo.stmItems.size
			removeFavourite(data)
			assert(fakeStmFavouritesRepo.stmItems.size == size - 1)
		}
	}

	@Test
	fun `Remove Stm Favourite to empty, expect same size`(){
		runBlocking {
			val data = fakeStmFavouritesRepo.stmItems.random()
			fakeStmFavouritesRepo.stmItems.clear()
			removeFavourite(data)
			assert(fakeStmFavouritesRepo.stmItems.size == 0)
		}
	}

	@Test
	fun `Remove Stm Favourite not existing, expect same size`(){
		runBlocking {
			val data = StmBusItem("foo", "bar", "newDir", 10, "last")
			val size = fakeStmFavouritesRepo.stmItems.size
			removeFavourite(data)
			assert(fakeStmFavouritesRepo.stmItems.size == size)
		}
	}

	@Test
	fun `Remove Exo Bus Favourite to empty, expect size - 1`(){
		runBlocking {
			val data = fakeExoFavouritesRepo.exoBusItems.random()
			val size = fakeExoFavouritesRepo.exoBusItems.size
			removeFavourite(data)
			assert(fakeExoFavouritesRepo.exoBusItems.size == size - 1)
		}
	}

	@Test
	fun `Remove Exo Bus Favourite not existing, expect same size`(){
		runBlocking {
			val data = fakeExoFavouritesRepo.exoBusItems.random()
			fakeExoFavouritesRepo.exoBusItems.clear()
			removeFavourite(data)
			assert(fakeExoFavouritesRepo.exoBusItems.size == 0)
		}
	}

	@Test
	fun `Remove Exo Bus Favourite to empty, expect same size`(){
		runBlocking {
			val data = ExoBusItem("foo", "bar", "newDir", "longName", "last")
			val size = fakeExoFavouritesRepo.exoBusItems.size
			removeFavourite(data)
			assert(fakeExoFavouritesRepo.exoBusItems.size == size)
		}
	}

	@Test
	fun `Remove Exo Train Favourite to empty, expect size - 1`(){
		runBlocking {
			val data = fakeExoFavouritesRepo.exoTrainItems.random()
			val size = fakeExoFavouritesRepo.exoTrainItems.size
			removeFavourite(data)
			assert(fakeExoFavouritesRepo.exoTrainItems.size == size - 1)
		}
	}

	@Test
	fun `Remove Exo Train Favourite not existing, expect same size`(){
		runBlocking {
			val data = fakeExoFavouritesRepo.exoTrainItems.random()
			fakeExoFavouritesRepo.exoTrainItems.clear()
			removeFavourite(data)
			assert(fakeExoFavouritesRepo.exoTrainItems.size == 0)
		}
	}

	@Test
	fun `Remove Exo Train Favourite to empty, expect same size`(){
		runBlocking {
			val data = ExoTrainItem("foo", "bar", "newDir", 10, "last", 10)
			val size = fakeExoFavouritesRepo.exoBusItems.size
			removeFavourite(data)
			assert(fakeExoFavouritesRepo.exoBusItems.size == size)
		}
	}
}