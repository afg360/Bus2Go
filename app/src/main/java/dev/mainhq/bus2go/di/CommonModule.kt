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
import dev.mainhq.bus2go.data.data_source.remote.CustomTrustManager
import dev.mainhq.bus2go.data.data_source.remote.NetworkMonitor
import dev.mainhq.bus2go.data.repository.AppStateRepositoryImpl
import dev.mainhq.bus2go.data.repository.DatabaseControllerRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoTrainFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.ExoTrainRepositoryImpl
import dev.mainhq.bus2go.data.repository.FavouritePositionRepositoryImpl
import dev.mainhq.bus2go.data.repository.NotificationRepositoryImpl
import dev.mainhq.bus2go.data.repository.SettingsRepositoryImpl
import dev.mainhq.bus2go.data.repository.StmFavouritesRepositoryImpl
import dev.mainhq.bus2go.data.repository.StmRepositoryImpl
import dev.mainhq.bus2go.domain.entity.DatabaseAgency
import dev.mainhq.bus2go.domain.use_case.AcceptSelfSignedCertificate
import dev.mainhq.bus2go.domain.use_case.CleanUpGarbageFiles
import dev.mainhq.bus2go.domain.use_case.ObserveDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.ScheduleDownloadDatabaseTask
import dev.mainhq.bus2go.domain.use_case.settings.SaveAllNotifSettings
import dev.mainhq.bus2go.domain.use_case.db_state.CheckDatabaseUpdateRequired
import dev.mainhq.bus2go.domain.use_case.db_state.DeleteDatabase
import dev.mainhq.bus2go.domain.use_case.db_state.GetDatabaseExpiryDate
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
import java.io.File

class CommonModule(applicationContext: Context) {
	private val databaseDownloadScheduler = DatabaseDownloadSchedulerImpl(
		WorkManager.getInstance(applicationContext)
	)
	val scheduleDownloadDatabaseTask = ScheduleDownloadDatabaseTask(
		databaseDownloadScheduler,
	)


	private val databaseController = DatabaseControllerRepositoryImpl(
		applicationContext.dataDir,
		applicationContext.cacheDir
	)

	private val stmDatabase = databaseController.createInstance(applicationContext, DatabaseAgency.STM) as AppDatabaseSTM?
	private val stmRepository = StmRepositoryImpl(
		feedInfoDAO = stmDatabase?.feedInfoDao(),
		calendarDatesDAO = stmDatabase?.calendarDatesDao(),
		routesDAO = stmDatabase?.routesDao(),
		stopsDAO = stmDatabase?.stopDao(),
		stopsInfoDAO = stmDatabase?.stopsInfoDao(),
		tripsDAO = stmDatabase?.tripsDao()
	)

	private val localKeyStore = LocalKeyStore(applicationContext.filesDir)
	val customTrustManager = CustomTrustManager.build(localKeyStore)


	private val tagsHandler = TagsHandler.getInstance(applicationContext)

	private val stmFavouritesRepository = StmFavouritesRepositoryImpl(
		tagsHandler = tagsHandler,
		stmFavouritesDataStore = applicationContext.stmFavouritesDataStore,
	)

	private val exoDatabase = databaseController.createInstance(applicationContext, DatabaseAgency.EXO) as AppDatabaseExo?
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

	private val exoTrainRepository = ExoTrainRepositoryImpl(
		calendarDAO = exoDatabase?.calendarDao(),
		routesDAO = exoDatabase?.routesDao(),
		stopTimesDAO = exoDatabase?.stopTimesDao(),
		tripsDAO = exoDatabase?.tripsDao()
	)

	private val exoTrainFavouritesRepository = ExoTrainFavouritesRepositoryImpl(
		tagsHandler = tagsHandler,
		exoFavouritesDataStore = applicationContext.exoFavouritesDataStore,
	)

	private val favouritesPositionRepository = FavouritePositionRepositoryImpl (
		applicationContext.favouritesPositionDataStore
	)

	val appStateRepository = AppStateRepositoryImpl(
		appStateDataStore = applicationContext.appStateDataStore,
		localKeyStore = localKeyStore,
		databasesDir = File(applicationContext.dataDir, "databases"),
		filesDir = applicationContext.filesDir,
		repos = listOf(
			stmRepository,
			exoRepository
		)
	)

	val deleteDatabase = DeleteDatabase(
		databaseController,
		appStateRepository
	)

	val getAllTags = GetAllTags(
		stmFavouritesRepository
	)

	val addTag = AddTag(
		listOf(
			stmFavouritesRepository,
			exoFavouritesRepository,
			exoTrainFavouritesRepository
		)
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
		listOf(
			stmRepository,
			exoRepository,
			exoTrainRepository
		)
	)


	//FIXME not ideal to make them like this...
	val addFavourite = AddFavourite(
		listOf(
			stmFavouritesRepository,
			exoFavouritesRepository,
			exoTrainFavouritesRepository
		),
		favouritesPositionRepository
	)

	val removeFavourite = RemoveFavourite(
		listOf(
			stmFavouritesRepository,
			exoFavouritesRepository,
			exoTrainFavouritesRepository
		),
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

	val observeDownloadDatabaseTask = ObserveDownloadDatabaseTask(
		appStateRepository,
		databaseDownloadScheduler,
	)

	val getSettings = GetSettings(
		settingsRepository
	)

	val isFirstTimeAppLaunched = IsFirstTimeAppLaunched(
		appStateRepository
	)

	val getFavourites = GetFavourites(
		listOf(
			stmFavouritesRepository,
			exoFavouritesRepository,
			exoTrainFavouritesRepository
		)
	)

	val getFavouritesWithTimeData = GetFavouritesWithTimeData(
		listOf(
			stmRepository,
			exoRepository,
			exoTrainRepository
		),
		getFavourites,
		favouritesPositionRepository
	)

	val getTransitTime = GetTransitTime(
		listOf(
			stmRepository,
			exoRepository,
			exoTrainRepository
		)
	)

	val getDatabaseExpiryDate = GetDatabaseExpiryDate(
		listOf(
			stmRepository,
			exoRepository,
			exoTrainRepository
		)
	)

	val getDirections = GetDirections(
		listOf(
			stmRepository,
			exoRepository,
			exoTrainRepository,
		)
	)

	val getStopNames = GetStopNames(
		listOf(
			stmRepository,
			exoRepository,
			exoTrainRepository
		)
	)

	val cleanUpGarbageFiles = CleanUpGarbageFiles(
		appStateRepository
	)
}
