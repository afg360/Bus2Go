package dev.mainhq.bus2go

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlin.properties.Delegates

/** Base activity class defining the correct theme to apply */
open class BaseActivity : AppCompatActivity() {

    private var isDark by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isDark = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("dark-mode", true)
        AppThemeState.setTheme(isDark)
    }

    override fun onResume() {
        super.onResume()
        isDark = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("dark-mode", true)
        //if the theme changed, must destroy the activity and recreate it, which is already done with AppDelegate thing
        if (AppThemeState.hasThemeChanged(isDark))
            AppThemeState.setTheme(isDark)
    }
}