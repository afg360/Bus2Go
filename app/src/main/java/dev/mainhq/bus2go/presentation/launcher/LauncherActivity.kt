package dev.mainhq.bus2go.presentation.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.presentation.config.ConfigActivity
import dev.mainhq.bus2go.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Dummy activity used to decide which activity to first launch */
class LauncherActivity: BaseActivity() {

	private val launcherActivityViewModel: LauncherActivityViewModel by viewModels{
		object: ViewModelProvider.Factory{
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				return LauncherActivityViewModel(
					(this@LauncherActivity.application as Bus2GoApplication).commonModule.isFirstTimeAppLaunched
				) as T
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		lifecycleScope.launch(Dispatchers.Main) {
			repeatOnLifecycle(Lifecycle.State.STARTED){
				val isFirstTime = launcherActivityViewModel.isFirstTime.filterNotNull().first()
				if (isFirstTime){
					startActivity(Intent(applicationContext, ConfigActivity::class.java))
				}
				else startActivity(Intent(applicationContext, MainActivity::class.java))
				finish()
			}
		}
	}
}