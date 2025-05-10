package dev.mainhq.bus2go.presentation.config;

import android.content.Intent
import android.os.Bundle;
import androidx.activity.viewModels
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.data.data_source.local.datastore.app_state.appStateDataStore
import dev.mainhq.bus2go.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

//todo set lang
//todo set color theme and or icon
//todo set notifs on or off (mostly for updates)
//todo set autoupdate

class ConfigActivity : BaseActivity() {

    private val viewModel: ConfigSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_activity)

        //null when the first time the activity is created, otherwise not null
        if (savedInstanceState == null){
            viewModel.setFragment(FragmentUsed.WELCOME)
        }

        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.currentFragment.filterNotNull().collect{ fragmentToUse ->
                    val fragment = when(fragmentToUse){
                        FragmentUsed.WELCOME -> ConfigWelcomeFragment()
                        FragmentUsed.THEME -> ConfigThemeFragment()
                        FragmentUsed.SERVER -> ConfigServerFragment()
                        FragmentUsed.DATABASES -> ConfigDatabasesFragment()
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.configActivityFragmentContainer, fragment)
                        .commit()

                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.event.collect{ eventMessage ->
                    if (eventMessage){
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }

    }

    private suspend fun setApplicationState(){
        appStateDataStore.edit { settings ->
            settings[booleanPreferencesKey("isFirstTime")] = false
        }
    }

}
