package dev.mainhq.bus2go.domain.use_case

import dev.mainhq.bus2go.domain.use_case.favourites.AddFavourite
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavouritesWithTimeData
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import dev.mainhq.bus2go.domain.use_case.settings.IsRealTimeEnabled

data class FavouritesUseCases(
	val getFavouritesWithTimeData: GetFavouritesWithTimeData,
	val addFavourite: AddFavourite,
	val removeFavourite: RemoveFavourite,
	val isRealTimeEnabled: IsRealTimeEnabled
)
