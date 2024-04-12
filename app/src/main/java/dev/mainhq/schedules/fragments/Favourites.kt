package dev.mainhq.schedules.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.mainhq.schedules.R
import dev.mainhq.schedules.preferences.settingsDataStore
import kotlinx.coroutines.flow.*

val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name="settings")

class Favourites : Fragment(R.layout.fragment_favourites) {
    //first get user favourites data
    //then make the recycler adapter with that data (either that or display 'no favourites'
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val foo = stringPreferencesKey( "")
        //val bar = this.context?.getSharedPreferences("favourites", Context.MODE_PRIVATE)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val foo = ""
        return super.onCreateView(inflater, container, savedInstanceState)
    }

/*
    fun readFavourites() : Flow<Int> {
        //FIXME FOR NOW USE ASYNC INSTEAD OF MULTIPROCESS... NEED IT TO WORK
        val favourites: Flow<Int> = context?.settingsDataStore?.data?.map {
            settings ->
                // The exampleCounter property is generated from the proto schema.
                settings.busStop
                settings.busNum
            } ?: TODO("No data found")
        return favourites
    }

    suspend fun writeFavourites(busNum : Int, busStop : String) {
        context?.settingsDataStore?.updateData { currentSettings ->
            currentSettings.toBuilder().setBusNum(currentSettings.busNum).build()
            currentSettings.toBuilder().setBusStop(currentSettings.busStop).build()
        }
    }
*/
}