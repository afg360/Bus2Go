package dev.mainhq.bus2go.domain.entity.stm

sealed class DirectionInfo{
	abstract val tripHeadSign : String

	data class StmDirectionInfo(
		override val tripHeadSign : String,
		val directionId : Int
	): DirectionInfo()

	data class ExoBusDirectionInfo(
		override val tripHeadSign: String
	): DirectionInfo()
}
