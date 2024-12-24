package dev.mainhq.bus2go

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import dev.mainhq.bus2go.updates.UpdateManagerWorker.Companion.FILE_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.properties.Delegates

/** Base activity class defining the correct theme to apply */
open class BaseActivity : AppCompatActivity() {

    private var isDark by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        isDark = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("dark-mode", true)
        AppThemeState.setTheme(isDark)
        super.onCreate(savedInstanceState)

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