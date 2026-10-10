package app.bus2go.android.presentation.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.bus2go.android.Bus2GoApplication
import app.bus2go.android.presentation.base.BaseActivity
import app.bus2go.android.presentation.config.ConfigActivity
import app.bus2go.android.presentation.main.MainActivity
import app.bus2go.android.utils.launchViewModelCollectLatest
import kotlinx.coroutines.flow.filterNotNull

/** Dummy activity used to decide which activity to first launch */
class LauncherActivity: BaseActivity() {

	private val launcherActivityViewModel: LauncherActivityViewModel by viewModels{
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				@Suppress("UNCHECKED_CAST")
				return LauncherActivityViewModel(
					(this@LauncherActivity.application as Bus2GoApplication).commonModule.settingsRepository,
					(this@LauncherActivity.application as Bus2GoApplication).commonModule.isFirstTimeAppLaunched,
				) as T
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		launchViewModelCollectLatest(launcherActivityViewModel.isDarkMode.filterNotNull()){
			if (it) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
			else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
			launcherActivityViewModel.themeSet()
		}
		launchViewModelCollectLatest(launcherActivityViewModel.isFirstTime.filterNotNull()) {
			if (it) {
				startActivity(Intent(applicationContext, ConfigActivity::class.java))
			}
			else {
				//before starting activity, make sure to do necessary checks for dbs that may be downloaded but not extracted
				//while doing that, verify another background task is not trying to download a more updated version

				startActivity(Intent(applicationContext, MainActivity::class.java))
			}
			launcherActivityViewModel.firstThemeSet()
		}
		launchViewModelCollectLatest(launcherActivityViewModel.readyToFinish){
			if (it) finish()
		}
	}
}