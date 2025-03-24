package dev.mainhq.bus2go.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.use_case.FavouritesUseCases

class ChooseStopViewModel(
	private val favouritesUseCases: FavouritesUseCases
): ViewModel() {

	fun addFavourite(favouriteTransitData: FavouriteTransitData){

	}

	fun removeFavourite(favouriteTransitData: FavouriteTransitData){

	}

}