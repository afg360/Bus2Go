package dev.mainhq.bus2go.presentation.core.state

import androidx.appcompat.app.AppCompatDelegate
import kotlin.properties.Delegates

//FIXME place this state caching in the data layer...
/** Singleton used to control the global theme of the application */
object AppThemeState {

    //FIXME if the app is not destroyed for a while but person set up the thing for a while, might
    //not work as expected...
    /**
     * Caches whether or not need to update local databases to avoid constantly looking up file.
     * Always initialised at launch at true
     **/
    var displayIsDbUpdatedDialog = true
        private set(shit) {
            displayIsDbUpdatedDialog = shit
        }

    fun turnOffDbUpdateChecking(){ displayIsDbUpdatedDialog = false }
}