package dev.mainhq.schedules.fragments

import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.schedules.R
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.preferences.BusInfo
import dev.mainhq.schedules.preferences.FavouritesData
import dev.mainhq.schedules.preferences.SettingsSerializer
import dev.mainhq.schedules.utils.Time
import dev.mainhq.schedules.utils.adapters.*
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/** The datastore of favourites refers to favourites defined in the preferences file, at dev.mainhq.schedules.preferences,
 *  NOT THIS FRAGMENT */
val Context.dataStore : DataStore<FavouritesData> by dataStore(
    fileName = "favourites.json",
    serializer = SettingsSerializer
)

class Favourites : Fragment(R.layout.fragment_favourites) {
    var executor : ScheduledExecutorService? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val list =  context?.dataStore?.data?.first()?.list?.toList() ?: listOf()
            if (list.isEmpty()) setEmpty(view)
            else {
                val mutableList = toFavouriteBusInfoList(list)
                recyclerViewDisplay(view, mutableList)
            }
        }
        val appBar = (parentFragment as Home).view?.findViewById<AppBarLayout>(R.id.mainAppBar)
        /** This part allows us to press the back button when in selection mode of favourites to get out of it */
        val callback = object : OnBackPressedCallback(true) {
            /** Hides all the checkboxes of the items in the recyclerview, deselects them, and puts back the searchbar as the nav bar */
            override fun handleOnBackPressed() {
                val recyclerView = view.findViewById<RecyclerView>(R.id.favouritesRecyclerView)
                recyclerView.tag?.also {recTag ->
                    /** Establish original layout (i.e. margins) */
                    if (recTag as String == "selected"){
                        recyclerView.forEach {
                            (recyclerView.adapter as FavouritesListElemsAdapter).unSelect(it as ViewGroup)
                            it.findViewById<MaterialCheckBox>(R.id.favourites_check_box).visibility = View.GONE
                            setMargins(it.findViewById(R.id.favouritesDataContainer), 20, 20)
                        }
                        recyclerView.tag = "unselected"
                    }
                }
                appBar?.apply { changeAppBar(this) }
            }
        }
        //FIXME activity?. instead???
        requireActivity().onBackPressedDispatcher.addCallback(callback)

        val recyclerView : RecyclerView = requireView().findViewById(R.id.favouritesRecyclerView)
        /** This part allows us to update each recyclerview item from favourites in "real time", i.e. the user can see
         *  an updated time left displayed */
        recyclerView.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                executor = Executors.newSingleThreadScheduledExecutor()
                val copy = executor!!
                copy.scheduleAtFixedRate({
                    lifecycleScope.launch {
                        //todo could add some incertitude, and web requests here too
                        val tmpList = context?.dataStore?.data?.first()?.list?.toList() ?: listOf()
                        if (tmpList.isNotEmpty()) {
                            //FIXME we only want to change the time left data, NOT the background colors etc
                            val mutableList = toFavouriteBusInfoList(tmpList)
                            withContext(Dispatchers.Main){
                                //FIXME nullPointerException bug is at line below
                                val favouritesListElemsAdapter = recyclerView.adapter as FavouritesListElemsAdapter?
                                for (i in 0 until recyclerView.size) {
                                    favouritesListElemsAdapter?.updateTime(recyclerView[i] as ViewGroup, mutableList[i])
                                }
                            }
                        }
                        //FIXME WE COULD REMOVE THAT LINE OF CODE?
                        else setEmpty(view)
                    }
                }, 0, 1, TimeUnit.SECONDS) //TODO need it to be for android or java????
            }
        })
        selectAllFavourites(recyclerView)
        parentFragment?.view?.findViewById<LinearLayout>(R.id.removeItemsWidget)?.setOnClickListener {_ ->
            this.context?.also { context ->
                MaterialAlertDialogBuilder(context)
                    //.setTitle(resources.getString(R.string.title))
                    .setMessage(resources.getString(R.string.remove_confirmation_dialog_text))
                    .setNegativeButton(resources.getString(R.string.remove_confirmation_dialog_decline)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .setPositiveButton(resources.getString(R.string.remove_confirmation_dialog_accept)) { dialog, _ ->
                        val toRemoveList = mutableListOf<BusInfo>()
                        recyclerView.forEach {
                            if ((recyclerView.adapter as FavouritesListElemsAdapter).isSelected(it as ViewGroup)){
                                toRemoveList.add(busInfoFromView(it))
                            }
                        }
                        lifecycleScope.launch {
                            context.dataStore.updateData { favouritesData ->
                                favouritesData.copy(list = favouritesData.list.mutate {
                                    it.removeIf{busInfo -> toRemoveList.contains(busInfo) }
                                })
                            }
                            withContext(Dispatchers.Main){
                                recyclerViewDisplay(view, toFavouriteBusInfoList(context.dataStore.data.first().list.toList()))
                                appBar?.apply { changeAppBar(this) }
                                dialog.cancel()
                            }
                        }
                    }
                    .show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //FIXME DOES NOT SEEM TO PREVENT THE BELOW
        //Prevents an IllegalArgumentException when coming back to the activity
        executor?.shutdown()

        //TODO go back to normal mode if was in selection mode
    }


    private suspend fun setEmpty(view : View){
        //FIXME seems that changing too many times/too fast the fragment causes it to fuck up the context? -> crash because not attached?
        withContext(Dispatchers.Main){
            view.findViewById<MaterialTextView>(R.id.favourites_text_view).text = getText(R.string.no_favourites)
        }
    }

    /** Used to get the required data to make a list of favouriteBusInfo, adding dates to busInfo elements */
    private suspend fun toFavouriteBusInfoList(list : List<BusInfo>) : MutableList<FavouriteBusInfo> {
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

    private suspend fun recyclerViewDisplay(view : View, times : MutableList<FavouriteBusInfo>){
        return withContext(Dispatchers.Main){
            view.findViewById<MaterialTextView>(R.id.favourites_text_view).text = getText(R.string.favourites)
            val layoutManager = LinearLayoutManager(view.context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val recyclerViewTmp : RecyclerView? = view.findViewById(R.id.favouritesRecyclerView)
            recyclerViewTmp?.layoutManager = layoutManager
            //TODO need to improve that code to make it more safe
            recyclerViewTmp?.adapter = recyclerViewTmp?.let { FavouritesListElemsAdapter(times, it) }
        }
    }


    //TODO CODE REPETITION IS SHITTY (same at dev.mainhq.utils.adapters.FavouritesListElemsAdapter)
    /** Add an onclicklistener to the material checkbox of the selection part of nav bar */
    private fun selectAllFavourites(recyclerView: RecyclerView){
        (parentFragment as Home).view?.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox)
            ?.setOnClickListener {
                recyclerView.apply {
                    /** If user clicks when it is not already checked*/
                    if ((it as MaterialCheckBox).isChecked) {
                        forEach {
                            (adapter as FavouritesListElemsAdapter).select(it as ViewGroup)
                        }
                    }
                    else {
                        forEach {
                            (adapter as FavouritesListElemsAdapter).unSelect(it as ViewGroup)
                        }
                    }
                    (parentFragment as Home).view?.findViewById<MaterialTextView>(R.id.selectedNumsOfFavourites)
                        ?.text = (adapter as FavouritesListElemsAdapter).run {
                        val deleteItemsWidget = this@Favourites.parentFragment?.view?.findViewById<LinearLayout>(R.id.removeItemsWidget)
                        if (numSelected > 0) {
                            if (deleteItemsWidget?.visibility == View.GONE) deleteItemsWidget.visibility = View.VISIBLE
                            numSelected.toString()
                        }
                        else {
                            deleteItemsWidget?.visibility = View.GONE
                            recyclerView.context.getString(R.string.select_favourites_to_remove)
                        }
                    }
                }
            }
    }

    /** Hides the selection mode appBar and comes back to main bar (searchBar) */
    private fun changeAppBar(appBar : AppBarLayout){
        appBar.children.elementAt(1).also{
            it.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox).isChecked = false
            it.visibility = View.GONE
        }
        appBar.children.elementAt(0).visibility = View.VISIBLE
    }

    private fun busInfoFromView(view : ViewGroup) : BusInfo{
        return BusInfo(view.findViewById<MaterialTextView>(R.id.favouritesStopNameTextView).text.toString(),
            view.findViewById<MaterialTextView>(R.id.favouritesTripheadsignTextView).text.toString()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

data class FavouriteBusInfo(val busInfo: BusInfo, val arrivalTime : Time)