package dev.mainhq.bus2go.data.data_source.local.datastore

import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteTrainItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem

object PreferenceMapper {

	fun mapStmBusToDto(stmBus: StmBusItem): StmFavouriteBusItemDto {
		return StmFavouriteBusItemDto(
			stmBus.stopName,
			stmBus.routeId,
			stmBus.directionId,
			stmBus.direction,
			stmBus.lastStop
		)
	}

	fun mapStmBus(stmDto: StmFavouritesDataDto): List<StmBusItem>{
		return stmDto.listSTM.map {
			StmBusItem(
				it.routeId,
				it.stopName,
				it.direction,
				it.directionId,
				it.lastStop
			)
		}
	}

	fun mapExoBusToDto(exoBus: ExoBusItem): ExoFavouriteBusItemDto {
		return ExoFavouriteBusItemDto(
			exoBus.stopName,
			exoBus.routeId,
			exoBus.direction,
			exoBus.routeLongName
		)
	}

	fun mapExoBus(exoDto: ExoFavouritesDataDto): List<ExoBusItem>{
		return exoDto.listExo.map {
			ExoBusItem(
				it.routeId,
				it.stopName,
				it.direction,
				it.routeLongName,
			)
		}
	}

	fun mapExoTrainToDto(exoTrain: ExoTrainItem): ExoFavouriteTrainItemDto {
		return ExoFavouriteTrainItemDto(
			exoTrain.stopName,
			exoTrain.routeId,
			exoTrain.trainNum,
			exoTrain.routeName,
			exoTrain.directionId,
			exoTrain.direction
		)
	}

	fun mapExoTrain(exoDto: ExoFavouritesDataDto): List<ExoTrainItem>{
		return exoDto.listExoTrain.map {
			ExoTrainItem(
				it.routeId,
				it.stopName,
				it.direction,
				it.trainNum,
				it.routeName,
				it.directionId,
			)
		}
	}
}