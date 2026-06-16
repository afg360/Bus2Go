package dev.mainhq.bus2go.domain.entity

import java.util.UUID


sealed class FavouriteTransitData {
	abstract val id : UUID
	abstract val routeId : String
	abstract val stopName : String
	abstract val direction : String
	abstract val tags: List<Tag>

	companion object {
		/**
		 * Make a TransitData into a FavouriteTransitData by generating a new UUID
		 **/
		fun fromTransitDataToFavouriteTransitData(transitData: TransitData): FavouriteTransitData {
			return when(transitData) {
				is StmBusItem -> {
					StmBusFavouriteItem(
						UUID.randomUUID(),
						transitData.routeId,
						transitData.stopName,
						transitData.direction,
						transitData.tags,
						transitData.directionId,
						transitData.lastStop
					)
				}
				is ExoBusItem -> {
					ExoBusFavouriteItem(
						UUID.randomUUID(),
						transitData.routeId,
						transitData.stopName,
						transitData.direction,
						transitData.tags,
						transitData.routeLongName,
					)
				}
				is ExoTrainItem -> {
					ExoTrainFavouriteItem(
						UUID.randomUUID(),
						transitData.routeId,
						transitData.stopName,
						transitData.direction,
						transitData.tags,
						transitData.trainNum,
						transitData.routeName,
						transitData.directionId,
					)
				}
			}
		}

		//FIXME move that as an abstract method and implement it in each subclass
		fun fromFavouriteTransitDataToTransitData(favouriteTransitData: FavouriteTransitData): TransitData {
			return when(favouriteTransitData) {
				is ExoBusFavouriteItem -> {
					ExoBusItem(
						favouriteTransitData.routeId,
						favouriteTransitData.stopName,
						favouriteTransitData.direction,
						favouriteTransitData.tags,
						favouriteTransitData.routeLongName
					)
				}
				is ExoTrainFavouriteItem -> {
					ExoTrainItem(
						favouriteTransitData.routeId,
						favouriteTransitData.stopName,
						favouriteTransitData.direction,
						favouriteTransitData.tags,
						favouriteTransitData.trainNum,
						favouriteTransitData.routeName,
						favouriteTransitData.directionId
					)
				}
				is StmBusFavouriteItem -> {
					StmBusItem(
						favouriteTransitData.routeId,
						favouriteTransitData.stopName,
						favouriteTransitData.direction,
						favouriteTransitData.tags,
						favouriteTransitData.directionId,
						favouriteTransitData.lastStop
					)
				}
			}
		}
	}

	data class StmBusFavouriteItem(
		override val id : UUID,
		override val routeId : String,
		override val stopName: String,
		override val direction : String,
		override val tags: List<Tag>,
		val directionId: Int,
		val lastStop : String,
	) : FavouriteTransitData()

	data class ExoBusFavouriteItem(
		override val id : UUID,
		override val routeId : String,
		override val stopName : String,
		override val direction: String,
		override val tags: List<Tag>,
		val routeLongName: String,
		//val headsign: String
	) : FavouriteTransitData()

	data class ExoTrainFavouriteItem(
		override val id : UUID,
		override val routeId : String,
		override val stopName : String,
		override val direction : String,
		override val tags: List<Tag>,
		val trainNum : Int,
		val routeName : String,
		val directionId: Int
	) : FavouriteTransitData()
}