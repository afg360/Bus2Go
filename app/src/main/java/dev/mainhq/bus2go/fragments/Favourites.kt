package dev.mainhq.bus2go.fragments

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.utils.Time
import dev.mainhq.bus2go.adapters.FavouritesListElemsAdapter
import dev.mainhq.bus2go.adapters.setMargins
import dev.mainhq.bus2go.preferences.TrainInfo
import dev.mainhq.bus2go.preferences.TransitInfo
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class Favourites(private val favouritesViewModel: FavouritesViewModel,
    private val roomViewModel : RoomViewModel) : Fragment(R.layout.fragment_favourites) {

    var executor : ScheduledExecutorService? = null
    var updateJob : Job? = null
    private lateinit var listener : ViewTreeObserver.OnGlobalLayoutListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                val list = toFavouriteTransitInfoList(listSTM, TransitAgency.STM) + toFavouriteTransitInfoList(listExo, TransitAgency.EXO_OTHER) +
                        toFavouriteTransitInfoList(listTrain, TransitAgency.EXO_TRAIN)
                println(list.toString())
                withContext(Dispatchers.Main){
                    view.findViewById<MaterialTextView>(R.id.favourites_text_view).text = getText(R.string.favourites)
                    val layoutManager = LinearLayoutManager(view.context)
                    layoutManager.orientation = LinearLayoutManager.VERTICAL
                    val recyclerViewTmp : RecyclerView? = view.findViewById(R.id.favouritesRecyclerView)
                    recyclerViewTmp?.layoutManager = layoutManager
                    //TODO need to improve that code to make it more safe
                    recyclerViewTmp?.tag = "unselected"
                    recyclerViewTmp?.adapter = recyclerViewTmp?.let { FavouritesListElemsAdapter(list, WeakReference(it) ) }
                }

            }
        }

        val appBar = (parentFragment as Home).view?.findViewById<AppBarLayout>(R.id.mainAppBar)
        /** This part allows us to press the back button when in selection mode of favourites to get out of it */

        //FIXME activity?. instead???
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
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
        })

        val recyclerView : RecyclerView = view.findViewById(R.id.favouritesRecyclerView)
        /** This part allows us to update each recyclerview item from favourites in "real time", i.e. the user can see
         *  an updated time left displayed */
        listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                executor = Executors.newSingleThreadScheduledExecutor()
                //TODO check if inrealtime is on.
                //FIXME this part crashes/creates a memory leak!!!!!!!
                executor!!.scheduleAtFixedRate({
                    val tmpListSTM = favouritesViewModel.stmBusInfo.value
                    val tmpListExo = favouritesViewModel.exoBusInfo.value
                    val tmpListTrain = favouritesViewModel.exoTrainInfo.value
                    if (tmpListSTM.isNotEmpty() || tmpListExo.isNotEmpty() || tmpListTrain.isNotEmpty()) {
                        //FIXME we only want to change the time left data, NOT the background colors etc
                        lifecycleScope.launch{
                            val mutableList = toFavouriteTransitInfoList(tmpListSTM, TransitAgency.STM) + toFavouriteTransitInfoList(tmpListExo, TransitAgency.EXO_OTHER) +
                                    toFavouriteTransitInfoList(tmpListTrain, TransitAgency.EXO_TRAIN)
                            withContext(Dispatchers.Main){
                                //FIXME nullPointerException bug is at line below
                                val favouritesListElemsAdapter = recyclerView.adapter as FavouritesListElemsAdapter?
                                for (i in 0 until recyclerView.size) {
                                    favouritesListElemsAdapter?.updateTime(recyclerView[i] as ViewGroup, mutableList[i])
                                }
                            }
                        }
                        //FIXME WE COULD REMOVE THAT LINE OF CODE?
                        //else setEmpty(view)
                    } }, 0, 1, TimeUnit.SECONDS)
            } }
        recyclerView.viewTreeObserver?.addOnGlobalLayoutListener(listener)

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
                        val toRemoveList = mutableListOf<TransitInfo>()
                        //TODO add agencies to know from which list to remove
                        recyclerView.forEach {
                            if ((recyclerView.adapter as FavouritesListElemsAdapter).isSelected(it as ViewGroup)){
                                toRemoveList.add(busInfoFromView(it))
                            }
                        }
                        lifecycleScope.launch {
                            favouritesViewModel.removeFavouriteBuses(toRemoveList)
                            val list = toFavouriteTransitInfoList(favouritesViewModel.stmBusInfo.value, TransitAgency.STM) +
                                    toFavouriteTransitInfoList(favouritesViewModel.exoBusInfo.value, TransitAgency.EXO_OTHER) +
                                    toFavouriteTransitInfoList(favouritesViewModel.exoTrainInfo.value, TransitAgency.EXO_TRAIN)
                                    recyclerViewDisplay(view, list, new = true)
                        }
                        appBar?.apply { changeAppBar(this) }
                        dialog.dismiss()
                    }
                    .show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        //recyclerView.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
        updateJob?.cancel()
        executor?.shutdown()
    }

    /** Used to get the required data to make a list of favouriteBusInfo, adding dates to busInfo elements */
    private suspend fun toFavouriteTransitInfoList(list : List<TransitInfo>, agency: TransitAgency) : MutableList<FavouriteTransitInfo> {
        val times : MutableList<FavouriteTransitInfo> = mutableListOf()
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
        return roomViewModel.getFavouriteStopTimes(list, agency, dayString, calendar, times)
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

    //find a way to get data for trains as welll
    private fun busInfoFromView(view : ViewGroup) : TransitInfo {
        return when (view.tag) {
            TransitAgency.EXO_TRAIN -> {
                TrainInfo(view.findViewById<MaterialTextView>(R.id.favouritesStopNameTextView).text.toString(),
                    view.findViewById<MaterialTextView>(R.id.favouritesTripheadsignTextView).text.toString(),
                    view.findViewById<MaterialTextView>(R.id.favouritesTripheadsignTextView).tag as Int)
            }
            TransitAgency.STM, TransitAgency.EXO_OTHER -> {
                BusInfo(
                    view.findViewById<MaterialTextView>(R.id.favouritesStopNameTextView).text.toString(),
                    view.findViewById<MaterialTextView>(R.id.favouritesTripheadsignTextView).text.toString()
                )
            }
            else -> throw IllegalStateException("The item view tag has not been initialised!!!")
        }
    }

}

data class FavouriteTransitInfo(val transitInfo: TransitInfo, val arrivalTime : Time?, val agency : TransitAgency)