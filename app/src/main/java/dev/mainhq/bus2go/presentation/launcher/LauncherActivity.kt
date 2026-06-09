package dev.mainhq.bus2go.presentation.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.presentation.config.ConfigActivity
import dev.mainhq.bus2go.presentation.main.MainActivity
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest
import kotlinx.coroutines.flow.filterNotNull

/** Dummy activity used to decide which activity to first launch */
class LauncherActivity: BaseActivity() {

	private val launcherActivityViewModel: LauncherActivityViewModel by viewModels{
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return LauncherActivityViewModel(
					(this@LauncherActivity.application as Bus2GoApplication).commonModule.isFirstTimeAppLaunched,
					(this@LauncherActivity.application as Bus2GoApplication).commonModule.getSettings
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