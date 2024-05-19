package dev.mainhq.schedules

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import kotlin.properties.Delegates

/** Base activity class defining the correct theme to apply */
open class BaseActivity : AppCompatActivity() {

    protected var isDark by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isDark = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("dark-mode", true)
        AppThemeState.setTheme(this, isDark)
    }

    override fun onResume() {
        super.onResume()
        //if the theme changed, must destroy the activity and recreate it
        isDark = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("dark-mode", true)
        if (AppThemeState.hasThemeChanged(isDark)){
            finish()
            startActivity(intent)
        }
    }
}