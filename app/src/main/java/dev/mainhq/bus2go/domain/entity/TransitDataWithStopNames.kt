package dev.mainhq.bus2go.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
/**
 * Used to communicate between ChooseDirection and ChooseStop.
 **/
sealed class TransitDataWithStopNames: Parcelable {
	abstract val stopNames: List<String>
	//val direction
}

@Parcelize
data class ExoBusTransitDataWithStopNames(
	val exoBusItem: ExoBusItem,
	override val stopNames: List<String>
): TransitDataWithStopNames()

@Parcelize
data class ExoTrainTransitDataWithStopNames(
	val exoTrainItem: ExoTrainItem,
	override val stopNames: List<String>
): TransitDataWithStopNames()

@Parcelize
data class StmBusTransitDataWithStopNames(
	val stmBusItem: StmBusItem,
	override val stopNames: List<String>
): TransitDataWithStopNames()
