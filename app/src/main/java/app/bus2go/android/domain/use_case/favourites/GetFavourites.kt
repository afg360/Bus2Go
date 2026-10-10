package app.bus2go.android.domain.use_case.favourites

import app.bus2go.android.domain.entity.FavouriteTransitData
import app.bus2go.android.domain.entity.TransitType
import app.bus2go.android.domain.repository.FavouritesRepository
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