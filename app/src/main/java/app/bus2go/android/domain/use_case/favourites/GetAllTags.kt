package app.bus2go.android.domain.use_case.favourites

import app.bus2go.android.domain.entity.Tag
import app.bus2go.android.domain.repository.FavouritesRepository

class GetAllTags(
	private val favouritesRepository: FavouritesRepository
){

	//FIXME not only 1 possible favouritesRepo...
	suspend operator fun invoke(): List<Tag> {
		return favouritesRepository.getTags()
	}

}