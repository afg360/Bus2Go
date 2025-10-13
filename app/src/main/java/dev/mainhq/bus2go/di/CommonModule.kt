package dev.mainhq.bus2go.di

import android.content.Context
import dev.mainhq.bus2go.data.core.LoggerImpl
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.data.data_source.local.datastore.app_state.appStateDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.exoFavouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.stmFavouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.data.data_source.notifications.NotificationHandler
import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.data.repository.AppStateRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoRepositoryImpl
import dev.mainhq.bus2go.data.repository.NotificationRepositoryImpl
import dev.mainhq.bus2go.data.repository.SettingsRepositoryImpl
import dev.mainhq.bus2go.data.repository.StmFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.StmRepositoryImpl
import dev.mainhq.bus2go.domain.use_case.settings.SaveAllNotifSettings
import dev.mainhq.bus2go.domain.use_case.settings.SaveBus2GoServer
import dev.mainhq.bus2go.domain.use_case.db_state.CheckDatabaseUpdateRequired
import dev.mainhq.bus2go.domain.use_case.db_state.IsFirstTimeAppLaunched
import dev.mainhq.bus2go.domain.use_case.db_state.SetDatabaseExpirationDate
import dev.mainhq.bus2go.domain.use_case.db_state.SetUpdateDbDialogLastAsToday
import dev.mainhq.bus2go.domain.use_case.db_state.WasUpdateDialogShownToday
import dev.mainhq.bus2go.domain.use_case.favourites.AddFavourite
import dev.mainhq.bus2go.domain.use_case.favourites.AddTag
import dev.mainhq.bus2go.domain.use_case.favourites.GetAllTags
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavourites
import dev.mainhq.bus2go.domain.use_case.favourites.GetFavouritesWithTimeData
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import dev.mainhq.bus2go.domain.use_case.settings.GetSettings
import dev.mainhq.bus2go.domain.use_case.transit.GetDirections
import dev.mainhq.bus2go.domain.use_case.transit.GetMinDateForUpdate
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime

class CommonModule(applicationContext: Context) {

	private val stmDatabase = AppDatabaseSTM.getInstance(applicationContext)
	private val stmRepository = StmRepositoryImpl(
		calendarDAO = stmDatabase?.calendarDao(),
		calendarDatesDAO = stmDatabase?.calendarDatesDao(),
		routesDAO = stmDatabase?.routesDao(),
		stopsDAO = stmDatabase?.stopDao(),
		stopsInfoDAO = stmDatabase?.stopsInfoDao(),
		tripsDAO = stmDatabase?.tripsDao()
	)

	val appStateRepository = AppStateRepositoryImpl(
		appStateDataStore = applicationContext.appStateDataStore,
		dataDir = applicationContext.dataDir
	)

	private val tagsHandler = TagsHandler(applicationContext)

	private val stmFavouritesRepository = StmFavouritesRepositoryImpl(
		tagsHandler = tagsHandler,
		stmFavouritesDataStore = applicationContext.stmFavouritesDataStore
	)

	private val exoDatabase = AppDatabaseExo.getInstance(applicationContext)
	private val exoRepository = ExoRepositoryImpl(
		calendarDAO = exoDatabase?.calendarDao(),
		routesDAO = exoDatabase?.routesDao(),
		stopTimesDAO = exoDatabase?.stopTimesDao(),
		tripsDAO = exoDatabase?.tripsDao()
	)

	private val exoFavouritesRepository = ExoFavouritesRepositoryImpl(
		tagsHandler = tagsHandler,
		exoFavouritesDataStore = applicationContext.exoFavouritesDataStore
	)

	val getAllTags = GetAllTags(
		stmFavouritesRepository
	)

	val addTag = AddTag(
		stmFavouritesRepository,
		exoFavouritesRepository
	)

	val settingsRepository = SettingsRepositoryImpl(
		appContext = applicationContext
	)

	private val notificationHandler = NotificationHandler(applicationContext)
	val notificationsRepository = NotificationRepositoryImpl(notificationHandler)

	val loggerImpl = LoggerImpl()
	val networkMonitor = NetworkMonitor.getInstance(applicationContext, loggerImpl)

	val saveBus2GoServer = SaveBus2GoServer(settingsRepository)
	val saveAllNotifSettings = SaveAllNotifSettings(settingsRepository, appStateRepository)

	val getRouteInfo = GetRouteInfo(
		exoRepository,
		stmRepository
	)

	val getFavouritesWithTimeData = GetFavouritesWithTimeData(
		exoFavouritesRepository,
		exoRepository,
		stmFavouritesRepository,
		stmRepository
	)

	//FIXME not ideal to make them like this...
	val addFavourite = AddFavourite(
		exoFavouritesRepository,
		stmFavouritesRepository
	)

	val removeFavourite = RemoveFavourite(
		exoFavouritesRepository,
		stmFavouritesRepository
	)

	val setDatabaseExpirationDate = SetDatabaseExpirationDate(appStateRepository)
	val checkDatabaseUpdateRequired = CheckDatabaseUpdateRequired(
		appStateRepository,
		setDatabaseExpirationDate,
		GetMinDateForUpdate(exoRepository, stmRepository)
	)
	val wasUpdateDialogShownToday = WasUpdateDialogShownToday(
		appStateRepository
	)
	val setUpdateDbDialogLastAsToday = SetUpdateDbDialogLastAsToday(
		appStateRepository
	)

	val getSettings = GetSettings(
		settingsRepository
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