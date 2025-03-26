package dev.mainhq.bus2go.presentation.settings

import android.os.Bundle
import com.google.android.material.appbar.MaterialToolbar
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val menuBar = findViewById<MaterialToolbar>(R.id.settingsToolBar)
        menuInflater.inflate(R.menu.app_bar_settings, menuBar.menu)
        menuBar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.settingsBackButton -> {
                    finish()
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.preferencesFragmentContainer, SettingsPreferences())
            .commit()
    }

    fun changeTheme(){
        finish()
        startActivity(intent)
    }

}
