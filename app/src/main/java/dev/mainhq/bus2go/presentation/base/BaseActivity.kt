package dev.mainhq.bus2go.presentation.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import dev.mainhq.bus2go.presentation.core.state.AppThemeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

//FIXME garbage code not supposed to be inside activities...
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