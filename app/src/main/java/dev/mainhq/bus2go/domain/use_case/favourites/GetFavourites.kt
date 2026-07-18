package dev.mainhq.bus2go.domain.use_case.favourites

import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.TransitType
import dev.mainhq.bus2go.domain.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class GetFavourites(
	private val favouritesRepos: List<FavouritesRepository>,
) {

	operator fun invoke(): Flow<Map<TransitType, List<FavouriteTransitData>>> {
		return combine(
			flows = favouritesRepos.map { repo -> repo.favourites.map { repo.transitType to it } }
		) { repos ->
			repos.toMap()
		}
	}
}