package dev.mainhq.bus2go.di

import android.content.Context
import androidx.work.WorkManager
import dev.mainhq.bus2go.data.backgroundtask.DatabaseDownloadSchedulerImpl
import dev.mainhq.bus2go.data.core.LoggerImpl
import dev.mainhq.bus2go.data.data_source.local.LocalKeyStore
import dev.mainhq.bus2go.data.data_source.local.database.exo.AppDatabaseExo
import dev.mainhq.bus2go.data.data_source.local.database.stm.AppDatabaseSTM
import dev.mainhq.bus2go.data.data_source.local.datastore.app_state.appStateDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.exo.exoFavouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.favourites_position.favouritesPositionDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.stm.stmFavouritesDataStore
import dev.mainhq.bus2go.data.data_source.local.datastore.tags.TagsHandler
import dev.mainhq.bus2go.data.data_source.notifications.NotificationHandler
import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.data.repository.AppStateRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoRepositoryImpl
import dev.mainhq.bus2go.data.repository.FavouritePositionRepositoryImpl
import dev.mainhq.bus2go.data.repository.NotificationRepositoryImpl
import dev.mainhq.bus2go.data.repository.SettingsRepositoryImpl
import dev.mainhq.bus2go.data.repository.StmFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.StmRepositoryImpl
import dev.mainhq.bus2go.domain.use_case.AcceptSelfSignedCertificate
import dev.mainhq.bus2go.domain.use_case.CleanUpGarbageFiles
import dev.mainhq.bus2go.domain.use_case.ObserveDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.settings.SaveAllNotifSettings
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
import dev.mainhq.bus2go.domain.use_case.favourites.MoveFavourite
import dev.mainhq.bus2go.domain.use_case.favourites.RemoveFavourite
import dev.mainhq.bus2go.domain.use_case.settings.GetSettings
import dev.mainhq.bus2go.domain.use_case.transit.GetDirections
import dev.mainhq.bus2go.domain.use_case.transit.GetRouteInfo
import dev.mainhq.bus2go.domain.use_case.transit.GetStopNames
import dev.mainhq.bus2go.domain.use_case.transit.GetTransitTime

class CommonModule(applicationContext: Context) {
	private val databaseDownloadScheduler = DatabaseDownloadSchedulerImpl(
		WorkManager.getInstance(applicationContext)
	)
	val scheduleDownloadDatabaseTask = ScheduleDownloadDatabaseTask(
		databaseDownloadScheduler,
	)

	val observeDownloadDatabaseTask = ObserveDownloadDatabaseTask(
		databaseDownloadScheduler,
	)

	private val stmDatabase = AppDatabaseSTM.getInstance(applicationContext)
	private val stmRepository = StmRepositoryImpl(
		feedInfoDAO = stmDatabase?.feedInfoDao(),
		calendarDatesDAO = stmDatabase?.calendarDatesDao(),
		routesDAO = stmDatabase?.routesDao(),
		stopsDAO = stmDatabase?.stopDao(),
		stopsInfoDAO = stmDatabase?.stopsInfoDao(),
		tripsDAO = stmDatabase?.tripsDao()
	)

	private val localKeyStore = LocalKeyStore(applicationContext.filesDir)

	val appStateRepository = AppStateRepositoryImpl(
		appStateDataStore = applicationContext.appStateDataStore,
		localKeyStore = localKeyStore,
		dataDir = applicationContext.dataDir,
		filesDir = applicationContext.filesDir
	)

	private val tagsHandler = TagsHandler.getInstance(applicationContext)

	private val stmFavouritesRepository = StmFavouritesRepositoryImpl(
		tagsHandler = tagsHandler,
		stmFavouritesDataStore = applicationContext.stmFavouritesDataStore,
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
		exoFavouritesDataStore = applicationContext.exoFavouritesDataStore,
	)

	private val favouritesPositionRepository = FavouritePositionRepositoryImpl (
		applicationContext.favouritesPositionDataStore
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

	val saveAllNotifSettings = SaveAllNotifSettings(settingsRepository, appStateRepository)

	val acceptSelfSignedCertificate = AcceptSelfSignedCertificate(
		appStateRepository = appStateRepository
	)

	val getRouteInfo = GetRouteInfo(
		exoRepository,
		stmRepository
	)


	//FIXME not ideal to make them like this...
	val addFavourite = AddFavourite(
		stmFavouritesRepository,
		exoFavouritesRepository,
		favouritesPositionRepository
	)

	val removeFavourite = RemoveFavourite(
		stmFavouritesRepository,
		exoFavouritesRepository,
		favouritesPositionRepository
	)

	val moveFavourite = MoveFavourite(
		favouritesPositionRepository
	)

	val setDatabaseExpirationDate = SetDatabaseExpirationDate(appStateRepository)
	val checkDatabaseUpdateRequired = CheckDatabaseUpdateRequired(
		appStateRepository,
		listOf(exoRepository, stmRepository)
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
		stmFavouritesRepository,
		exoFavouritesRepository
	)

	val getFavouritesWithTimeData = GetFavouritesWithTimeData(
		getFavourites,
		stmRepository,
		exoRepository,
		favouritesPositionRepository
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

	val cleanUpGarbageFiles = CleanUpGarbageFiles(
		appStateRepository
	)
}
