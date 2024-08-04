package dev.mainhq.bus2go.fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.preferences.ExoBusData
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.adapters.FavouritesListElemsAdapter
import dev.mainhq.bus2go.adapters.setMargins
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.utils.getDayString
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel
import dev.mainhq.bus2go.viewmodels.RealTimeViewModel
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference


class Favourites(private val favouritesViewModel: FavouritesViewModel,
    private val roomViewModel : RoomViewModel) : Fragment(R.layout.fragment_favourites) {

    private lateinit var recyclerView : RecyclerView
    //private lateinit var listener : ViewTreeObserver.OnGlobalLayoutListener
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var realTimeViewModel: RealTimeViewModel
    private var isUpdating = true

    private enum class State{
        LOST, AVAILABLE, UNAVAILABLE, LOSING
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realTimeViewModel = ViewModelProvider(requireActivity())[RealTimeViewModel::class.java]
        realTimeViewModel.loadDomainName(requireActivity().application)
        val isRealtimeEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean("real-time-data", false)
        lifecycleScope.launch {
            favouritesViewModel.loadData()
            val listSTM = favouritesViewModel.stmBusInfo.value
            val listExo = favouritesViewModel.exoBusInfo.value
            val listTrain = favouritesViewModel.exoTrainInfo.value
            if (listSTM.isEmpty() && listExo.isEmpty() && listTrain.isEmpty()) {
                withContext(Dispatchers.Main){
                    view.findViewById<MaterialTextView>(R.id.favourites_text_view).text =
                        getText(R.string.no_favourites)
                }
            }
            else {
                //check if connected to internet
                val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val state = callbackFlow {
                    val networkCalleBack = object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network : Network) {
                            super.onAvailable(network)
                            Log.e(TAG, "The default network is now: $network")
                            launch { send(State.AVAILABLE) }
                        }

                        override fun onLosing(network: Network, maxMsToLive: Int) {
                            super.onLosing(network, maxMsToLive)
                            launch { send(State.LOSING) }
                        }

                        override fun onLost(network : Network) {
                            super.onLost(network)
                            Log.e(TAG, "The application no longer has a default network. The last default network was $network")
                            launch { send(State.LOST) }
                        }

                        override fun onUnavailable() {
                            super.onUnavailable()
                            launch { send(State.UNAVAILABLE) }
                        }
                    }
                    connectivityManager.registerDefaultNetworkCallback(networkCalleBack)
                    awaitClose {
                        connectivityManager.unregisterNetworkCallback(networkCalleBack)
                    }
                }
                launch(Dispatchers.IO) {
                    state.collect {
                        //put inside a fxn so that it can be cancelled and call back in onPause and onResume (or wtv in a fragment)
                        /** This part allows us to update each recyclerview item from favourites in "real time", i.e. the user can see
                         *  an updated time left displayed */
                        //FIXME this may be wrong because wifi connectivity might be changing, doesnt account for that
                        //doesnt update properly, when deleted doesnt register at the right time
                        //also keeps doing requests even if the server is off (perhaps could momentarily pause these requests even if raltimeenabled for optimisations
                        while(isUpdating){
                            if (isRealtimeEnabled && it == State.AVAILABLE){
                                val list = toFavouriteTransitInfoList(this, listSTM, TransitAgency.STM, true) +
                                        toFavouriteTransitInfoList(this, listExo, TransitAgency.EXO_OTHER, true) +
                                        toFavouriteTransitInfoList(this, listTrain, TransitAgency.EXO_TRAIN, true)
                                
                                recyclerViewDisplay(view, list, true)
                            }
                            else{
                                val list = toFavouriteTransitInfoList(this, listSTM, TransitAgency.STM) +
                                        toFavouriteTransitInfoList(this, listExo, TransitAgency.EXO_OTHER) +
                                        toFavouriteTransitInfoList(this, listTrain, TransitAgency.EXO_TRAIN)
                                println(list.toString())
                                //if not, do the below
                                recyclerViewDisplay(view, list, true)
                            }
                            delay(1000)
                        }
                        
                    }
                }.join()
            }
        }
        val appBar = (parentFragment as Home).view?.findViewById<AppBarLayout>(R.id.mainAppBar)
        /** This part allows us to press the back button when in selection mode of favourites to get out of it */
        onBackPressedCallback = object : OnBackPressedCallback(true) {
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
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)

        recyclerView = view.findViewById(R.id.favouritesRecyclerView)
        
        selectAllFavouritesOnClickListener(recyclerView)

        parentFragment?.view?.findViewById<LinearLayout>(R.id.removeItemsWidget)?.setOnClickListener {_ ->
            this.context?.also { context ->
                MaterialAlertDialogBuilder(context)
                    //.setTitle(resources.getString(R.string.title))
                    .setMessage(resources.getString(R.string.remove_confirmation_dialog_text))
                    .setNegativeButton(resources.getString(R.string.remove_confirmation_dialog_decline)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .setPositiveButton(resources.getString(R.string.remove_confirmation_dialog_accept)) { dialog, _ ->
                        val toRemoveList = mutableListOf<TransitData>()
                        //TODO add agencies to know from which list to remove
                        recyclerView.forEach {
                            if ((recyclerView.adapter as FavouritesListElemsAdapter).isSelected(it as ViewGroup)){
                                toRemoveList.add(busInfoFromView(it))
                            }
                        }
                        lifecycleScope.launch {
                            favouritesViewModel.removeFavourites(toRemoveList)
                            val list = (toFavouriteTransitInfoList(this, favouritesViewModel.stmBusInfo.value, TransitAgency.STM, false)
                                    + toFavouriteTransitInfoList(this, favouritesViewModel.exoBusInfo.value, TransitAgency.EXO_OTHER, false)
                                    + toFavouriteTransitInfoList(this, favouritesViewModel.exoTrainInfo.value, TransitAgency.EXO_TRAIN, false))
                                    recyclerViewDisplay(view, list, new = true)
                        }
                        appBar?.apply { changeAppBar(this) }
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        isUpdating = true
        //call the update fxn again?
    }
    
    override fun onStop() {
        super.onStop()
        isUpdating = false
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.remove()
    }


    /** Used to get the required data to make a list of favouriteTransitInfo, adding dates to transitInfo elements */
    private suspend fun toFavouriteTransitInfoList(coroutineScope: CoroutineScope, list : List<TransitData>, agency: TransitAgency, realTimeEnabled : Boolean = false) : List<FavouriteTransitInfo> {
        val times : MutableList<FavouriteTransitInfo> = mutableListOf()
        val calendar = Calendar.getInstance()
        val dayString = getDayString(calendar)
        return if (realTimeEnabled){
            //need to make it a pair with the corresponding FavouriteTransitInfo
            val jobs = list.map { Pair(
                coroutineScope.async(Dispatchers.IO) { roomViewModel.getFavouriteStopTime(it, agency, dayString, calendar) },
                coroutineScope.async(Dispatchers.IO) { realTimeViewModel.getArrivalTimes(agency.toString(), it.routeId, it.direction, it.stopName) }
            )}
            jobs.map{
                val staticData = it.first.await()
                //fixme, tmp only last one choosen
                val realTimes = it.second.await()
                if (realTimes.isEmpty()) staticData
                else {
                    var toKeep : Time = realTimes.first()//realTimes.last()
                    realTimes.forEach{ realTime ->
                            staticData.arrivalTime?.also { staticData ->
                                val foo = (staticData - realTime)
                                val bar = (staticData - toKeep)
                                //FIXME wont work properly when the actual realtime data has been updated
                                if (foo == null || bar == null) toKeep = staticData
                                else if (foo < bar) toKeep = realTime
                            }
                    }
                    FavouriteTransitInfo(staticData.transitData, toKeep, agency)
                }
            }
        }
        else roomViewModel.getFavouriteStopTimes(list, agency, dayString, calendar, times)
    }

    private suspend fun recyclerViewDisplay(view : View, times : List<FavouriteTransitInfo>, new : Boolean = false){
        withContext(Dispatchers.Main){
            if (times.isEmpty()){
                view.findViewById<MaterialTextView>(R.id.favourites_text_view).text =
                    getText(R.string.no_favourites)
            }
            else {
                view.findViewById<MaterialTextView>(R.id.favourites_text_view).text =
                    getText(R.string.favourites)
            }
            val layoutManager = LinearLayoutManager(view.context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val recyclerViewTmp : RecyclerView? = view.findViewById(R.id.favouritesRecyclerView)
            recyclerViewTmp?.layoutManager = layoutManager
            //TODO need to improve that code to make it more safe
            if (new) recyclerViewTmp?.tag = "unselected"
            recyclerViewTmp?.adapter = recyclerViewTmp?.let { FavouritesListElemsAdapter(times, WeakReference(it) ) }
        }
    }

    //TODO CODE REPETITION IS SHITTY (same at dev.mainhq.utils.adapters.FavouritesListElemsAdapter)
    /** Add an onclicklistener to the material checkbox of the selection part of nav bar */
    private fun selectAllFavouritesOnClickListener(recyclerView: RecyclerView){
        (parentFragment as Home).view?.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox)
            ?.setOnClickListener {
                recyclerView.apply {
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

    //find a way to get data for trains as well
    private fun busInfoFromView(view : ViewGroup) : TransitData {
        return when (view.tag) {
            is ExoBusData -> view.tag as ExoBusData
            is StmBusData -> view.tag as StmBusData
            is TrainData -> view.tag as TrainData
            else -> throw IllegalStateException("The item view tag has not been initialised!!!")
        }
    }
}

data class FavouriteTransitInfo(val transitData: TransitData, val arrivalTime : Time?, val agency : TransitAgency)