package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.data.repository.FakeExoFavouritesRepo
import dev.mainhq.bus2go.data.repository.FakeStmFavouritesRepo
import org.junit.Before

class GetFavouritesTest {

	private lateinit var fakeExoFavouritesRepo: FakeExoFavouritesRepo
	private lateinit var fakeStmFavouritesRepo: FakeStmFavouritesRepo
	private lateinit var addFavourite: AddFavourite

	@Before
	fun setup(){
		fakeExoFavouritesRepo = FakeExoFavouritesRepo()
		fakeStmFavouritesRepo = FakeStmFavouritesRepo()
		addFavourite = AddFavourite(fakeExoFavouritesRepo, fakeStmFavouritesRepo)
	}

}