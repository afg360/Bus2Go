package dev.mainhq.bus2go

import androidx.appcompat.app.AppCompatDelegate
import kotlin.properties.Delegates

/** Singleton used to control the global theme of the application */
object AppThemeState {

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