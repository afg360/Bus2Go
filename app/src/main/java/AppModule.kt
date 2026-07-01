import android.content.Context
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.data.repository.DatabaseDownloadRepositoryImpl
import dev.mainhq.bus2go.domain.use_case.settings.CheckIsBus2GoServer

class AppModule(applicationContext: Context) {

	val dbDownloadRepository = DatabaseDownloadRepositoryImpl(
		applicationContext as Bus2GoApplication,
		applicationContext.commonModule.settingsRepository,
		applicationContext.commonModule.networkMonitor,
	)

	val checkIsBus2GoServer = CheckIsBus2GoServer(dbDownloadRepository)

}