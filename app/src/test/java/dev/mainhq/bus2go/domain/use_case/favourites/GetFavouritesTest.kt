package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.data.repository.FakeExoFavouritesRepo
import dev.mainhq.bus2go.data.repository.FakeStmFavouritesRepo
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetFavouritesTest {

	//TODO sorting when the time comes...

	private lateinit var fakeExoFavouritesRepo: FakeExoFavouritesRepo
	private lateinit var fakeStmFavouritesRepo: FakeStmFavouritesRepo
	private lateinit var getFavourites: GetFavourites

	@Before
	fun setup(){
		fakeExoFavouritesRepo = FakeExoFavouritesRepo()
		fakeStmFavouritesRepo = FakeStmFavouritesRepo()
		getFavourites = GetFavourites(fakeExoFavouritesRepo, fakeStmFavouritesRepo)
	}

	@Test
	fun `Get All Favourites, expect size = sum of all agencies`(){
		runBlocking {
			assert(getFavourites().size == fakeStmFavouritesRepo.stmItems.size + fakeExoFavouritesRepo.exoTrainItems.size + fakeExoFavouritesRepo.exoBusItems.size)
		}
	}

	@Test
	fun `Get All Favourites when no stm, expect size = all exo`(){
		runBlocking {
			fakeStmFavouritesRepo.stmItems.clear()
			assert(getFavourites().size == fakeExoFavouritesRepo.exoTrainItems.size + fakeExoFavouritesRepo.exoBusItems.size)
		}
	}

	@Test
	fun `Get All Favourites when no exoBus, expect size = stm + exoTrains`(){
		runBlocking {
			fakeExoFavouritesRepo.exoBusItems.clear()
			assert(getFavourites().size == fakeStmFavouritesRepo.stmItems.size + fakeExoFavouritesRepo.exoTrainItems.size)
		}
	}

	@Test
	fun `Get All Favourites when no exoTrain, expect size = stm + exoBus`(){
		runBlocking {
			fakeExoFavouritesRepo.exoTrainItems.clear()
			assert(getFavourites().size == fakeStmFavouritesRepo.stmItems.size + fakeExoFavouritesRepo.exoBusItems.size)
		}
	}

	@Test
	fun `Get All Favourites when none exist, expect size = 0`(){
		runBlocking {
			fakeStmFavouritesRepo.stmItems.clear()
			fakeExoFavouritesRepo.exoTrainItems.clear()
			fakeExoFavouritesRepo.exoBusItems.clear()
			assert(getFavourites().isEmpty())
		}
	}

}