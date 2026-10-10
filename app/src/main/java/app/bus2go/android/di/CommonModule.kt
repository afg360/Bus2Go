package app.bus2go.android.di

import android.content.Context
import androidx.work.WorkManager
import app.bus2go.android.data.backgroundtask.DatabaseDownloadSchedulerImpl
import app.bus2go.android.data.data_source.local.LocalKeyStore
import app.bus2go.android.data.data_source.local.datastore.app_state.appStateDataStore
import app.bus2go.android.data.data_source.local.datastore.exo.exoFavouritesDataStore
import app.bus2go.android.data.data_source.local.datastore.favourites_position.favouritesPositionDataStore
import app.bus2go.android.data.data_source.local.datastore.stm.stmFavouritesDataStore
import app.bus2go.android.data.data_source.local.datastore.tags.TagsHandler
import app.bus2go.android.data.data_source.notifications.NotificationHandler
import app.bus2go.android.data.data_source.remote.CustomTrustManager
import app.bus2go.android.data.repository.AppStateRepositoryImpl
import app.bus2go.android.data.repository.DatabaseControllerRepositoryImpl
import app.bus2go.android.data.repository.ExoFavouritesRepositoryImpl
import app.bus2go.android.data.repository.ExoRepositoryImpl
import app.bus2go.android.data.repository.ExoTrainFavouritesRepositoryImpl
import app.bus2go.android.data.repository.ExoTrainRepositoryImpl
import app.bus2go.android.data.repository.FavouritePositionRepositoryImpl
import app.bus2go.android.data.repository.NotificationRepositoryImpl
import app.bus2go.android.data.repository.SettingsRepositoryImpl
import app.bus2go.android.data.repository.StmFavouritesRepositoryImpl
import app.bus2go.android.data.repository.StmRepositoryImpl
import app.bus2go.android.domain.core.Logger
import app.bus2go.android.domain.use_case.AcceptSelfSignedCertificate
import app.bus2go.android.domain.use_case.CancelDownloadDatabaseTask
import app.bus2go.android.domain.use_case.CleanUpGarbageFiles
import app.bus2go.android.domain.use_case.DeleteSelfSignedCertificates
import app.bus2go.android.domain.use_case.ObserveDownloadDatabaseTask
import app.bus2go.android.domain.use_case.ScheduleDownloadDatabaseTask
import app.bus2go.android.domain.use_case.settings.SaveAllNotifSettings
import app.bus2go.android.domain.use_case.db_state.CheckDatabaseUpdateRequired
import app.bus2go.android.domain.use_case.db_state.DeleteDatabase
import app.bus2go.android.domain.use_case.db_state.GetDatabaseExpiryDate
import app.bus2go.android.domain.use_case.db_state.IsFirstTimeAppLaunched
import app.bus2go.android.domain.use_case.db_state.SetDatabaseExpirationDate
import app.bus2go.android.domain.use_case.db_state.SetUpdateDbDialogLastAsToday
import app.bus2go.android.domain.use_case.db_state.WasUpdateDialogShownToday
import app.bus2go.android.domain.use_case.favourites.AddFavourite
import app.bus2go.android.domain.use_case.favourites.AddTag
import app.bus2go.android.domain.use_case.favourites.GetAllTags
import app.bus2go.android.domain.use_case.favourites.GetFavourites
import app.bus2go.android.domain.use_case.favourites.GetFavouritesWithTimeData
import app.bus2go.android.domain.use_case.favourites.MoveFavourite
import app.bus2go.android.domain.use_case.favourites.RemoveFavourite
import app.bus2go.android.domain.use_case.settings.GetSettings
import app.bus2go.android.domain.use_case.transit.GetDirections
import app.bus2go.android.domain.use_case.transit.GetRouteInfo
import app.bus2go.android.domain.use_case.transit.GetStopNames
import app.bus2go.android.domain.use_case.transit.GetTransitTime
import java.io.File

class CommonModule(
	applicationContext: Context,
	logger: Logger?
) {
	private val databaseDownloadScheduler = DatabaseDownloadSchedulerImpl(
		WorkManager.getInstance(applicationContext)
	)

	val scheduleDownloadDatabaseTask = ScheduleDownloadDatabaseTask(
		databaseDownloadScheduler,
	)

	val cancelDownloadDatabaseTask = CancelDownloadDatabaseTask(
		databaseDownloadScheduler,
	)

	//FIXME THIS CODE IS SUPER FRAGILE but works for now...
	private val databaseControllerRepository = DatabaseControllerRepositoryImpl
		.getInstance(applicationContext, logger)

	private val stmRepository = StmRepositoryImpl(
		databaseControllerRepository
	)

	private val localKeyStore = LocalKeyStore(applicationContext.filesDir)
	val customTrustManager = CustomTrustManager.build(localKeyStore)

	private val tagsHandler = TagsHandler.getInstance(applicationContext)

	private val stmFavouritesRepository = StmFavouritesRepositoryImpl(
		tagsHandler = tagsHandler,
		stmFavouritesDataStore = applicationContext.stmFavouritesDataStore,
	)

	private val exoRepository = ExoRepositoryImpl(
		databaseControllerRepository
	)

	private val exoFavouritesRepository = ExoFavouritesRepositoryImpl(
		tagsHandler = tagsHandler,
		exoFavouritesDataStore = applicationContext.exoFavouritesDataStore,
	)

	private val exoTrainRepository = ExoTrainRepositoryImpl(
		databaseControllerRepository
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
		databaseControllerRepository,
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

	val saveAllNotifSettings = SaveAllNotifSettings(settingsRepository, appStateRepository)

	val acceptSelfSignedCertificate = AcceptSelfSignedCertificate(
		appStateRepository = appStateRepository
	)

	val deleteSelfSignedCertificates = DeleteSelfSignedCertificates(
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
