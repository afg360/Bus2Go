package dev.mainhq.schedules.fragments

import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

val Context.dataStore : DataStore<Favourites> by dataStore(
    fileName = "favourites.json",
    serializer = SettingsSerializer
)

class Favourites : Fragment(R.layout.fragment_favourites) {
    var executor : ScheduledExecutorService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favourites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val list =  context?.dataStore?.data?.first()?.list?.toList()
            if (list == null){
                TODO("")
            } else if (list.isEmpty()) {
                setEmpty(view)
            } else {
                val mutableList = setBus(list, view)
                recyclerViewDisplay(view, mutableList)
            }

        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val recyclerView = view.findViewById<RecyclerView>(R.id.favouritesRecyclerView)
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

        val recyclerView : RecyclerView = requireView().findViewById(R.id.favouritesRecyclerView)
        recyclerView.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to prevent multiple calls
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                executor = Executors.newSingleThreadScheduledExecutor()
                val copy = executor!!
                // Once RecyclerView is laid out, update its contents
                // Schedule a task to update the TextView every 10 seconds
                copy.scheduleAtFixedRate({
                    lifecycleScope.launch {
                        //todo could add some incertitude, and web requests here too
                        val tmpList = context?.dataStore?.data?.first()?.list?.toList()
                            ?: listOf()
                        if (tmpList.isNotEmpty()) {
                            //FIXME we only want to change the time left data, NOT the background colors etc
                            val mutableList = setBus(tmpList, requireView())
                            withContext(Dispatchers.Main){
                                val favouritesListElemsAdapter = recyclerView.adapter as FavouritesListElemsAdapter
                                for (i in 0 until recyclerView.size) {
                                    favouritesListElemsAdapter.updateTime(recyclerView[i] as ViewGroup, mutableList[i])
                                }
                            }
                        }
                        //FIXME WE COULD REMOVE THAT LINE OF CODE
                        else setEmpty(requireView())
                    }
                }, 0, 1, TimeUnit.SECONDS) //TODO need it to be for android or java????
            }
        })
    }

    override fun onPause() {
        super.onPause()
        executor!!.shutdown()
    }

    private suspend fun setEmpty(view : View){
        withContext(Dispatchers.Main){
            view.findViewById<TextView>(R.id.favourites_text_view).text = getText(R.string.no_favourites)
        }
    }

    private suspend fun setBus(list : List<BusInfo>, view : View) : MutableList<FavouriteBusInfo> {
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
        return times
    }

    private suspend fun recyclerViewDisplay(view : View, times : MutableList<FavouriteBusInfo>,
                                            recyclerView: RecyclerView? = null){
        return withContext(Dispatchers.Main){
            view.findViewById<TextView>(R.id.favourites_text_view).text = getText(R.string.favourites)
            val layoutManager = LinearLayoutManager(view.context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val recyclerViewTmp : RecyclerView? = view.findViewById(R.id.favouritesRecyclerView)
            recyclerViewTmp?.layoutManager = layoutManager
            //need to improve that code to make it more safe
            recyclerViewTmp?.adapter = recyclerViewTmp?.let { FavouritesListElemsAdapter(times, it) }
            //return@withContext recyclerView
            /*
            else{
                //FIXME the task is being run async so error...
                val favouritesListElemsAdapter = recyclerView.adapter as FavouritesListElemsAdapter
                for (i in 0 until recyclerView.size){
                    favouritesListElemsAdapter.updateTime(recyclerView[i] as ViewGroup, times[i])
                }
            }*/
        }
    }

}

class SelectedFavourites : Fragment(R.layout.selected_favourites_checkbox){

}

data class FavouriteBusInfo(val busInfo: BusInfo, val arrivalTime : Time)