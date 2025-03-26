package dev.mainhq.bus2go.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
/** Entity to interface with data coming from dataStore */
sealed class FavouriteTransitData: Parcelable {
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
}

/**
 * @param routeId aka busNum.
 **/
data class StmFavouriteBusItem(
	override val routeId : String,
	override val stopName: String,
	override val direction : String,
	val directionId: Int,
	val lastStop : String
) : FavouriteTransitData()


data class ExoFavouriteBusItem(
	override val routeId : String,
	override val stopName : String,
	override val direction: String,
	val routeLongName: String,
	val headsign: String
) : FavouriteTransitData()

data class ExoFavouriteTrainItem(
	override val routeId : String,
	override val stopName : String,
	override val direction : String,
	val trainNum : Int,
	val routeName : String,
	val directionId: Int
) : FavouriteTransitData()