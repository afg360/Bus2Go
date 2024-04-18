package dev.mainhq.schedules.fragments

import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.schedules.R
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.preferences.BusInfo
import dev.mainhq.schedules.preferences.Favourites
import dev.mainhq.schedules.preferences.SettingsSerializer
import dev.mainhq.schedules.utils.Time
import dev.mainhq.schedules.utils.adapters.FavouritesListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val Context.dataStore : DataStore<Favourites> by dataStore(
    fileName = "favourites.json",
    serializer = SettingsSerializer
)

class Favourites : Fragment(R.layout.fragment_favourites) {
    //first get user favourites data
    //then make the recycler adapter with that data (either that or display 'no favourites'
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            val list =  context?.dataStore?.data?.first()?.list?.toList()
            if (list == null){
                TODO("TO IMPLEMENT")
            }
            else if (list.isEmpty()) setEmpty(view)
            else setBus(list, view)
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val recyclerView : RecyclerView = view.findViewById(R.id.favouritesRecyclerView)
                if (recyclerView.tag != null){
                    if (recyclerView.tag == "selected"){
                        recyclerView.forEach {
                            val viewGroup = it as ViewGroup
                            (recyclerView.adapter as FavouritesListElemsAdapter).unSelect(viewGroup)
                        }
                        recyclerView.tag = "unselected"
                    }
                }
                else{
                    super.handleOnBackCancelled()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private suspend fun setEmpty(view : View){
        withContext(Dispatchers.Main){
            view.findViewById<TextView>(R.id.favourites_text_view).text = getText(R.string.no_favourites)
        }
    }

    private suspend fun setBus(list : List<BusInfo>, view : View) {
        //todo find the first time data for every bus on the list
        val stopsInfoDAO = context?.applicationContext?.let {
            Room.databaseBuilder(it, AppDatabase::class.java, "stm_info.db")
                    .createFromAsset("database/stm_info.db").build() }?.stopsInfoDao()
        val times : MutableList<FavouriteBusInfo> = mutableListOf()
        val calendar = Calendar.getInstance()
        val dayString = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "d"
            Calendar.MONDAY -> "m"
            Calendar.TUESDAY -> "t"
            Calendar.WEDNESDAY -> "w"
            Calendar.THURSDAY -> "y"
            Calendar.FRIDAY -> "f"
            Calendar.SATURDAY -> "s"
            else -> null
        }
        dayString ?: throw IllegalStateException("Cannot have a non day of the week!")
        list.forEach {busInfo ->
            stopsInfoDAO?.getFavouriteStopTime(busInfo.stopName, dayString, Time(calendar).toString(), busInfo.tripHeadsign)
                ?.let { time -> times.add(FavouriteBusInfo(busInfo, time)) }
        }
        withContext(Dispatchers.Main){
            view.findViewById<TextView>(R.id.favourites_text_view).text = getText(R.string.favourites)
            val layoutManager = LinearLayoutManager(view.context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val recyclerView : RecyclerView? = view.findViewById(R.id.favouritesRecyclerView)
            recyclerView?.layoutManager = layoutManager
            //need to improve that code to make it more safe
            recyclerView?.adapter = recyclerView?.let { FavouritesListElemsAdapter(times, it) }
        }
    }

}

data class FavouriteBusInfo(val busInfo: BusInfo, val arrivalTime : Time)