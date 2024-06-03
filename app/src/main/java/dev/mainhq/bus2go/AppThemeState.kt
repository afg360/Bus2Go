package dev.mainhq.bus2go

import android.content.Context
import kotlin.properties.Delegates

object AppThemeState {

    var prevVal by Delegates.notNull<Boolean>()

    /** Basic init function to be called when setting up a theme
     *  on the oncreate of an activity */
    fun setTheme(activityContext : Context, isDark : Boolean){
        prevVal = isDark
        if (prevVal) activityContext.setTheme(R.style.MaterialTheme_Bus2Go_Dark)
        else activityContext.setTheme(R.style.MaterialTheme_Bus2Go)
    }

    /** Called to see if the theme of the whole app changed
     *  relative to the activity*/
    fun hasThemeChanged(/** This variable serves as seeing the
                            state of isDark changing */
                        isDark: Boolean) : Boolean{
        return (prevVal || isDark) && !(prevVal && isDark)
    }
}