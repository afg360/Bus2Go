package dev.mainhq.bus2go.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Entity to interface with data coming from dataStore */
sealed class TransitData: Parcelable {
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
}

@Parcelize
/**
 * @param routeId aka busNum.
 **/
data class StmBusItem(
	override val routeId : String,
	override val stopName: String,
	override val direction : String,
	val directionId: Int,
	val lastStop : String
) : TransitData()


@Parcelize
data class ExoBusItem(
	override val routeId : String,
	override val stopName : String,
	override val direction: String,
	val routeLongName: String,
	val headsign: String
) : TransitData()

@Parcelize
data class ExoTrainItem(
	override val routeId : String,
	override val stopName : String,
	override val direction : String,
	val trainNum : Int,
	val routeName : String,
	val directionId: Int
) : TransitData()
