package dev.mainhq.bus2go.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/** Entity to interface with data coming from dataStore */
sealed class TransitData: Parcelable {
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
	abstract val tags: List<Tag>

	fun compareMainAttr(other: TransitData): Boolean {
		return routeId == other.routeId && stopName == other.stopName && direction == other.direction
	}
}

fun <T: FavouriteTransitData, R: TransitData> Iterable<T>.compareTransitData(other: R): Boolean {
	this.forEach {
		if (FavouriteTransitData.fromFavouriteTransitDataToTransitData(it).compareMainAttr(other)){
			return true
		}
	}
	return false
}

/**
 * @return The FavouriteTransitData in the iterable equivalent to the input TransitData
 * */
fun <T: FavouriteTransitData, R: TransitData> Iterable<T>.getSameFavouriteItem(other: R): T? {
	this.forEach {
		if (FavouriteTransitData.fromFavouriteTransitDataToTransitData(it).compareMainAttr(other)){
			return it
		}
	}
	return null
}

@Parcelize
/**
 * @param routeId aka busNum.
 **/
data class StmBusItem(
	override val routeId : String,
	override val stopName: String,
	override val direction : String,
	override val tags: List<Tag>,
	val directionId: Int,
	val lastStop : String,
) : TransitData()


@Parcelize
/**
 * @param direction aka headsign
 * */
data class ExoBusItem(
	override val routeId : String,
	override val stopName : String,
	override val direction: String,
	override val tags: List<Tag>,
	val routeLongName: String,
	//val headsign: String
) : TransitData()

@Parcelize
data class ExoTrainItem(
	override val routeId : String,
	override val stopName : String,
	override val direction : String,
	override val tags: List<Tag>,
	val trainNum : Int,
	val routeName : String,
	val directionId: Int
) : TransitData()
