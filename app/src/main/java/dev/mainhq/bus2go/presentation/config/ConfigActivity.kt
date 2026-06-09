package dev.mainhq.bus2go.presentation.config;

import android.content.Intent
import android.os.Bundle;
import androidx.activity.viewModels
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.presentation.main.MainActivity
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest

//todo set lang
//todo set color theme and or icon
//todo set notifs on or off (mostly for updates)
//todo set autoupdate

class ConfigActivity : BaseActivity() {

    private val viewModel: ConfigSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_activity)

        launchViewModelCollectLatest(viewModel.currentFragment) { fragmentToUse ->
            if (fragmentToUse != null) {
                val fragment = when (fragmentToUse) {
                    FragmentUsed.WELCOME -> ConfigWelcomeFragment()
                    FragmentUsed.THEME -> ConfigThemeFragment()
                    FragmentUsed.SERVER -> ConfigServerFragment()
                    FragmentUsed.DATABASES -> ConfigDatabasesFragment()
                    FragmentUsed.NOTIFICATIONS -> ConfigNotificationsFragment()
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.configActivityFragmentContainer, fragment)
                    .commit()
            }
        }

        launchViewModelCollectLatest(viewModel.event){ eventMessage ->
            if (eventMessage){
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }
    }
}
