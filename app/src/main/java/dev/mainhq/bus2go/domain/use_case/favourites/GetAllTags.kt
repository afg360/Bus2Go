package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.Tag
import dev.mainhq.bus2go.domain.repository.FavouritesRepository

class GetAllTags(
	private val favouritesRepository: FavouritesRepository
){

	suspend operator fun invoke(): List<Tag> {
		return favouritesRepository.getTags()
	}

}