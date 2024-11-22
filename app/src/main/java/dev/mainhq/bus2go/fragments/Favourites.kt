package dev.mainhq.bus2go.fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class Favourites() : Fragment(R.layout.fragment_favourites) {

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var realTimeViewModel: RealTimeViewModel
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: NetworkCallback? = null
    //private var isUpdating = true
    private var isUsingRealTime = false
    private lateinit var favouritesViewModel: FavouritesViewModel
    private lateinit var roomViewModel: RoomViewModel

    private var executor : ScheduledExecutorService? = null
    private var scheduledTask: ScheduledFuture<*>? = null
    //may perhaps be not required?
    private var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favouritesViewModel = ViewModelProvider(requireActivity())[FavouritesViewModel::class.java]
        roomViewModel = ViewModelProvider(requireActivity())[RoomViewModel::class.java]

        realTimeViewModel = ViewModelProvider(requireActivity())[RealTimeViewModel::class.java]
        realTimeViewModel.loadDomainName(requireActivity().application)
        val isRealtimeEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .getBoolean("real-time-data", false)
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.favouritesRecyclerView)
        executor = Executors.newSingleThreadScheduledExecutor()
        
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
                connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mutableList = toFavouriteTransitInfoList(this, listSTM, TransitAgency.STM) + toFavouriteTransitInfoList(this, listExo, TransitAgency.EXO_OTHER) +
                        toFavouriteTransitInfoList(this, listTrain, TransitAgency.EXO_TRAIN)
                if (connectivityManager?.activeNetwork == null){
                    println("Not connected to the internet yet")
                    lifecycleScope.launch {
                        recyclerViewDisplay(view, mutableList, new=true)
                    }
                }
                val networkRequest = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    //.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .build()
                
                networkCallback = object: NetworkCallback() {
                    // do your websocketing shit here, not outside of it
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        println("A network became available")
                        //do some work in a thread?
                        lifecycleScope.launch {
                            if (isRealtimeEnabled){
                                isUsingRealTime = true
                                //getRealTime will never return if the connection is alright since it is an infinite loop
                                //need to deal with it with perhaps an exception
                                if (realTimeViewModel.getRealTime(listSTM) == 1) {
                                    Toast.makeText( context, "An error occured trying to connect to the bus2go server",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            else
                                recyclerViewDisplay(view, mutableList, new=true)
                        }
                    }
                    
                    // Network capabilities have changed for the network
                    override fun onCapabilitiesChanged( network: Network, networkCapabilities: NetworkCapabilities ) {
                        super.onCapabilitiesChanged(network, networkCapabilities)
                        val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                        //println("Network Capabilities have changed")
                    }
                    
                    // lost network connection
                    override fun onLost(network: Network) {
                        super.onLost(network)
                        println("Lost connection to network")
                        isUsingRealTime = false
                        //tmp solution for now. needs to adapt to data already received by server if it exists
                        lifecycleScope.launch {
                            recyclerViewDisplay(view, mutableList, new=true)
                        }
                    }
                    
                    override fun onUnavailable() {
                        super.onUnavailable()
                        println("Networks are unavailable")
                        isUsingRealTime = false
                        lifecycleScope.launch {
                            recyclerViewDisplay(view, mutableList, new=true)
                        }
                    }
                }
                connectivityManager!!.registerNetworkCallback(networkRequest, networkCallback!!)
            }
            //TODO check if in realtime is on.
            val weakRefRecyclerView = WeakReference(recyclerView)
            scheduledTask = executor?.scheduleWithFixedDelay({
                val tmpListSTM = favouritesViewModel.stmBusInfo.value
                val tmpListExo = favouritesViewModel.exoBusInfo.value
                val tmpListTrain = favouritesViewModel.exoTrainInfo.value
                
                if (tmpListSTM.isNotEmpty() || tmpListExo.isNotEmpty() || tmpListTrain.isNotEmpty()) {
                    job = lifecycleScope.launch{
                        //for now assume realtime doesn't work
                        val mutableList = toFavouriteTransitInfoList(this, tmpListSTM, TransitAgency.STM) +
                                            toFavouriteTransitInfoList(this, tmpListExo, TransitAgency.EXO_OTHER) +
                                            toFavouriteTransitInfoList(this, tmpListTrain, TransitAgency.EXO_TRAIN)
                        withContext(Dispatchers.Main){
                            weakRefRecyclerView.get()?.also {
                                val favouritesListElemsAdapter = it.adapter as FavouritesListElemsAdapter?
                                for (i in 0 until it.size) {
                                    favouritesListElemsAdapter?.updateTime(it[i] as ViewGroup, mutableList[i])
                                }
                            }
                        }
                    }
                }
            }, 0, 1, TimeUnit.SECONDS)
        }
        
        val appBar = (parentFragment as Home).view?.findViewById<AppBarLayout>(R.id.mainAppBar)
        /** This part allows us to press the back button when in selection mode of favourites to get out of it */
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            /** Hides all the checkboxes of the items in the recyclerview, deselects them, and puts back the searchbar as the nav bar */
            override fun handleOnBackPressed() {
                val innerRecyclerView = view.findViewById<RecyclerView>(R.id.favouritesRecyclerView)
                innerRecyclerView.tag?.also {recTag ->
                    /** Establish original layout (i.e. margins) */
                    if (recTag as String == "selected"){
                        innerRecyclerView.forEach {
                            (innerRecyclerView.adapter as FavouritesListElemsAdapter).unSelect(it as ViewGroup)
                            it.findViewById<MaterialCheckBox>(R.id.favourites_check_box).visibility = View.GONE
                            setMargins(it.findViewById(R.id.favouritesDataContainer), 20, 20)
                        }
                        innerRecyclerView.tag = "unselected"
                    }
                }
                appBar?.apply { changeAppBar(this) }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
        
        
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
    
    /*
    override fun onResume() {
        super.onResume()
        isUpdating = true
        //call the update fxn again?
    }
    
    override fun onStop() {
        super.onStop()
        isUpdating = false
    }
     */
    
    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.remove()
        networkCallback?.also{
            connectivityManager?.unregisterNetworkCallback(it)
        }
        
        job?.cancel()
        scheduledTask?.cancel(true)
        executor?.shutdown()
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