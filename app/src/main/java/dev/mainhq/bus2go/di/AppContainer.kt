package dev.mainhq.bus2go.di

import android.content.Context
import androidx.room.Room
import dev.mainhq.bus2go.data.core.LoggerImpl
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.data.data_source.local.datastore.app_state.appStateDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.exoFavouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.stmFavouritesDataStore
import dev.mainhq.bus2go.data.repository.AppStateRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoRepositoryImpl
import dev.mainhq.bus2go.data.repository.SettingsRepositoryImpl
import dev.mainhq.bus2go.data.repository.StmFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.StmRepositoryImpl
import dev.mainhq.bus2go.domain.use_case.FavouritesUseCases
import dev.mainhq.bus2go.domain.use_case.TransitTimeInfoUseCases
import dev.mainhq.bus2go.domain.use_case.db_state.CheckDatabaseUpdateRequired
import dev.mainhq.bus2go.domain.use_case.db_state.IsFirstTimeAppLaunched
import dev.mainhq.bus2go.domain.use_case.db_state.SetDatabaseState
import dev.mainhq.bus2go.domain.use_case.favourites.AddFavourite
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavourites
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavouritesWithTimeData
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import dev.mainhq.bus2go.domain.use_case.settings.IsRealTimeEnabled
import dev.mainhq.bus2go.domain.use_case.transit.GetDirections
import dev.mainhq.bus2go.domain.use_case.transit.GetMinDateForUpdate
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime

open class AppContainer(applicationContext: Context) {
	//TODO change back to a private val
	private val stmDatabase = AppDatabaseSTM.getInstance(applicationContext)
		/*
		Room.databaseBuilder(applicationContext, AppDatabaseSTM::class.java, AppDatabaseSTM.DATABASE_NAME)
		.createFromAsset(AppDatabaseSTM.DATABASE_PATH)
		.addMigrations(AppDatabaseSTM.MIGRATION_1_2)
		.build()

		 */

	private val stmRepository = StmRepositoryImpl(
		stmDatabase.calendarDao(),
		stmDatabase.calendarDatesDao(),
		stmDatabase.routesDao(),
		stmDatabase.stopDao(),
		stmDatabase.stopsInfoDao(),
		stmDatabase.tripsDao()
	)

	private val appStateRepository = AppStateRepositoryImpl(
		applicationContext.appStateDataStore,
		applicationContext.dataDir
	)

	private val stmFavouritesRepository = StmFavouritesRepositoryImpl(
		applicationContext.stmFavouritesDataStore
	)

	private val exoDatabase = Room.databaseBuilder(applicationContext, AppDatabaseExo::class.java, AppDatabaseExo.DATABASE_NAME)
		.createFromAsset(AppDatabaseExo.DATABASE_PATH)
		.addMigrations(AppDatabaseExo.MIGRATION_1_2)
		.build()

	private val exoRepository = ExoRepositoryImpl(
		exoDatabase.calendarDao(),
		exoDatabase.routesDao(),
		exoDatabase.stopTimesDao(),
		exoDatabase.tripsDao()
	)

	private val exoFavouritesRepository = ExoFavouritesRepositoryImpl(
		applicationContext.exoFavouritesDataStore
	)

	private val settingsRepository = SettingsRepositoryImpl(
		applicationContext
	)

	private val loggerImpl = LoggerImpl()

	val favouritesUseCases = FavouritesUseCases(
		GetFavouritesWithTimeData(
			exoFavouritesRepository,
			exoRepository,
			stmFavouritesRepository,
			stmRepository
		),
		AddFavourite(
			exoFavouritesRepository,
			stmFavouritesRepository
		),
		RemoveFavourite(
			exoFavouritesRepository,
			stmFavouritesRepository
		),
		IsRealTimeEnabled(
			settingsRepository
		)
	)

	val transitTimeInfoUseCases = TransitTimeInfoUseCases(
		GetRouteInfo(
			exoRepository,
			stmRepository,
		),
		GetStopNames(
			loggerImpl,
			exoRepository,
			stmRepository
		),
		GetTransitTime(
			exoRepository,
			stmRepository
		)
	)

	//FIXME not ideal to make them like this...
	val addFavourite = AddFavourite(
		exoFavouritesRepository,
		stmFavouritesRepository
	)

	val setDatabaseState = SetDatabaseState(appStateRepository)
	val checkDatabaseUpdateRequired = CheckDatabaseUpdateRequired(
		appStateRepository,
		setDatabaseState,
		GetMinDateForUpdate(exoRepository, stmRepository)
	)

	val isFirstTimeAppLaunched = IsFirstTimeAppLaunched(
		appStateRepository
	)

	val getFavourites = GetFavourites(
		exoFavouritesRepository,
		stmFavouritesRepository
	)

	val getTransitTime = GetTransitTime(
		exoRepository,
		stmRepository
	)

	val getDirections = GetDirections(
		exoRepository,
		stmRepository
	)

	val getStopNames = GetStopNames(
		loggerImpl,
		exoRepository,
		stmRepository
	)
}