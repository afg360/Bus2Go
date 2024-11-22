package dev.mainhq.bus2go

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import dev.mainhq.bus2go.fragments.preferences.SettingsPreferences

class Settings : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

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
