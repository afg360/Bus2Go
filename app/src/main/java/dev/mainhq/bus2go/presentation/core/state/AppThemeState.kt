package dev.mainhq.bus2go.presentation.core.state

import androidx.appcompat.app.AppCompatDelegate
import kotlin.properties.Delegates

//FIXME place this state caching in the data layer...
/** Singleton used to control the global theme of the application */
object AppThemeState {

    //FIXME if the app is not destroyed for a while but person set up the thing for a while, might
    //not work as expected...
    private var _displayIsDbUpdatedDialog: Boolean = true

    /**
     * Caches whether or not need to update local databases to avoid constantly looking up file.
     * Always initialised at launch at true
     **/
    val displayIsDbUpdatedDialog: Boolean get() = _displayIsDbUpdatedDialog

    fun turnOffDbUpdateChecking(){ _displayIsDbUpdatedDialog = false }

    private var prevVal by Delegates.notNull<Boolean>()

    /** Basic init function to be called when setting up a theme
     *  on the oncreate of an activity */
    fun setTheme(isDark : Boolean){
        prevVal = isDark
        if (prevVal) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /** Called to see if the theme of the whole app changed
     *  relative to the activity*/
    fun hasThemeChanged(/** This variable serves as seeing the
                            state of isDark changing */
                        isDark: Boolean) : Boolean{
        return (prevVal || isDark) && !(prevVal && isDark)
    }
}