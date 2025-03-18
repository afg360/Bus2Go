package dev.mainhq.bus2go.data.data_source.local.preference

import dev.mainhq.bus2go.data.data_source.local.preference.exo.entity.ExoFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.preference.exo.entity.ExoFavouriteTrainItemDto
import dev.mainhq.bus2go.data.data_source.local.preference.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.preference.stm.entity.StmFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.preference.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.exo.ExoFavouriteTrainItem
import dev.mainhq.bus2go.domain.entity.stm.StmFavouriteBusItem

object Mapper {

	fun mapStmBusToDto(stmBus: StmFavouriteBusItem): StmFavouriteBusItemDto {
		return StmFavouriteBusItemDto(
			stmBus.stopName,
			stmBus.routeId,
			stmBus.directionId,
			stmBus.direction,
			stmBus.lastStop
		)
	}

	fun mapStmBus(stmDto: StmFavouritesDataDto): List<StmFavouriteBusItem>{
		return stmDto.listSTM.map {
			StmFavouriteBusItem(
				it.routeId,
				it.stopName,
				it.direction,
				it.directionId,
				it.lastStop
			)
		}
	}

	fun mapExoBusToDto(exoBus: ExoFavouriteBusItem): ExoFavouriteBusItemDto {
		return ExoFavouriteBusItemDto(
			exoBus.stopName,
			exoBus.routeId,
			exoBus.direction,
			exoBus.routeLongName,
			exoBus.headsign
		)
	}

	fun mapExoBus(exoDto: ExoFavouritesDataDto): List<ExoFavouriteBusItem>{
		return exoDto.listExo.map {
			ExoFavouriteBusItem(
				it.routeId,
				it.stopName,
				it.direction,
				it.routeLongName,
				it.headsign
			)
		}
	}

	fun mapExoTrainToDto(exoTrain: ExoFavouriteTrainItem): ExoFavouriteTrainItemDto {
		return ExoFavouriteTrainItemDto(
			exoTrain.stopName,
			exoTrain.routeId,
			exoTrain.trainNum,
			exoTrain.routeName,
			exoTrain.directionId,
			exoTrain.direction
		)
	}

	fun mapExoTrain(exoDto: ExoFavouritesDataDto): List<ExoFavouriteTrainItem>{
		return exoDto.listExoTrain.map {
			ExoFavouriteTrainItem(
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