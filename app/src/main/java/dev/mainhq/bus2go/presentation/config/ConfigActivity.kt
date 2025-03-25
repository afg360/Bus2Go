package dev.mainhq.bus2go.presentation.config;

import android.os.Bundle;
import androidx.activity.viewModels
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.mainhq.bus2go.BaseActivity
import dev.mainhq.bus2go.R

//todo set lang
//todo set color theme and or icon
//todo set notifs on or off (mostly for updates)
//todo set autoupdate

class ConfigActivity : BaseActivity() {

    private val viewModel: ConfigurationStateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_activity)

        supportFragmentManager.beginTransaction()
                .replace(
                    R.id.configActivityFragmentContainer,
                    supportFragmentManager.findFragmentByTag(viewModel.currentFragmentTag.value)
                        ?: ConfigWelcomeFragment()
                )
                .commit()
        viewModel.event.observe(this){ eventMessage ->
            if (eventMessage){
                finish()
                //val intent = Intent(applicationContext, MainActivity::class.java)
                //intent.putExtra("first", false)
                //startActivity(intent)
            }
        }
        //setup the fragments
    }


    private suspend fun setApplicationState(){
        applicationStateDataStore.edit { settings ->
            settings[booleanPreferencesKey("isFirstTime")] = false
        }
    }
}
