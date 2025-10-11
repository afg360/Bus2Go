package dev.mainhq.bus2go.data.data_source.local.datastore

import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouriteTrainItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.entity.ExoFavouritesDataDto
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.StmFavouriteBusItemDto_v1
import dev.mainhq.bus2go.data.data_source.local.datastore.deprecated.StmFavouritesDataDto1
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouriteBusItemDto
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.entity.StmFavouritesDataDto
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem

object PreferenceMapper {

	fun mapStmBusToDto(stmBus: StmBusItem): StmFavouriteBusItemDto {
		return StmFavouriteBusItemDto(
			stopName = stmBus.stopName,
			routeId = stmBus.routeId,
			direction = stmBus.direction,
			tags = stmBus.tags,
			directionId = stmBus.directionId,
			lastStop = stmBus.lastStop,
		)
	}

	fun mapStmBus(stmDto: StmFavouritesDataDto): List<StmBusItem>{
		return stmDto.listSTM.map {
			StmBusItem(
				routeId = it.routeId,
				stopName = it.stopName,
				direction = it.direction,
				tags =it.tags,
				directionId = it.directionId,
				lastStop = it.lastStop,
			)
		}
	}

	fun mapExoBusToDto(exoBus: ExoBusItem): ExoFavouriteBusItemDto {
		return ExoFavouriteBusItemDto(
			stopName = exoBus.stopName,
			routeId = exoBus.routeId,
			direction = exoBus.direction,
			tags = exoBus.tags,
			routeLongName = exoBus.routeLongName
		)
	}

	fun mapExoBus(exoDto: ExoFavouritesDataDto): List<ExoBusItem>{
		return exoDto.listExo.map {
			ExoBusItem(
				routeId = it.routeId,
				stopName = it.stopName,
				direction = it.direction,
				tags = it.tags,
				routeLongName = it.routeLongName,
			)
		}
	}

	fun mapExoTrainToDto(exoTrain: ExoTrainItem): ExoFavouriteTrainItemDto {
		return ExoFavouriteTrainItemDto(
			stopName = exoTrain.stopName,
			routeId = exoTrain.routeId,
			direction = exoTrain.direction,
			tags = exoTrain.tags,
			trainNum = exoTrain.trainNum,
			routeName = exoTrain.routeName,
			directionId = exoTrain.directionId,
		)
	}

	fun mapExoTrain(exoDto: ExoFavouritesDataDto): List<ExoTrainItem>{
		return exoDto.listExoTrain.map {
			ExoTrainItem(
				routeId = it.routeId,
				stopName = it.stopName,
				direction = it.direction,
				tags = it.tags,
				trainNum = it.trainNum,
				routeName = it.routeName,
				directionId = it.directionId,
			)
		}
	}
}