package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.MainActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.TimesActivity
import dev.mainhq.bus2go.fragments.FavouriteTransitInfo
import dev.mainhq.bus2go.fragments.Home
import dev.mainhq.bus2go.preferences.ExoBusData
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.utils.Time
import java.lang.ref.WeakReference
import java.time.LocalTime

class FavouritesListElemsAdapter(private val list : List<FavouriteTransitInfo>, recyclerView: WeakReference<RecyclerView>)
    : RecyclerView.Adapter<FavouritesListElemsAdapter.ViewHolder>(){

    var numSelected : Int = 0
    private val recyclerView = recyclerView.get()!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //FIXME check if parent can provide me the recyclerview instead
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.favourites_list_elem, parent, false), recyclerView //see if other way...
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = list[position]

        holder.arrivalTimeTextView.text = info.arrivalTime?.getTimeString()
        if (info.arrivalTime == null){
            holder.timeRemainingTextView.text =
                holder.itemView.context.getString(R.string.none_for_the_rest_of_the_day)
        }
        else{
            holder.timeRemainingTextView.text = getTimeRemaining(info.arrivalTime)
            //if (info.arrivalTime.timeRemaining()?.compareTo(Time(0,3,59)) == -1)
            if (info.arrivalTime < Time(LocalTime.of(0, 3, 59)))
                holder.timeRemainingTextView.setTextColor(holder.itemView.resources.getColor(R.color.red, null))
            else {
                holder.timeRemainingTextView.setTextColor(MaterialColors.getColor(holder.itemView, android.R.attr.editTextColor))
            }
        }

        when(info.agency) {
            TransitAgency.EXO_TRAIN -> {
                info.transitData as TrainData
                holder.routeLongNameTextView.text = "" //clear it out even if gone
                holder.routeLongNameTextView.visibility = GONE
                holder.directionTextView.text = holder.itemView.context
                    .getString(R.string.train_to, info.transitData.trainNum.toString(), info.transitData.direction)
                holder.itemView.tag = info.transitData
                holder.stopNameTextView.text = info.transitData.stopName
                holder.tripHeadsignTextView.text = info.transitData.routeName
                //FIXME for testing purposes, the below is added
                holder.tripHeadsignTextView.tag = info.transitData.direction
                holder.tripHeadsignTextView.setTextColor(
                    holder.itemView.resources
                        .getColor(R.color.orange, null)
                )
                holder.stopNameTextView.setTextColor(
                    holder.itemView.resources
                        .getColor(R.color.orange, null)
                )
                holder.directionTextView.setTextColor(holder.itemView.resources
                    .getColor(R.color.orange,null))
            }

            TransitAgency.STM -> {
                info.transitData as StmBusData
                holder.routeLongNameTextView.text = "" //clear it out even if gone
                holder.routeLongNameTextView.visibility = GONE
                holder.itemView.tag = info.transitData
                holder.directionTextView.text = "To ${info.transitData.lastStop}"
                holder.tripHeadsignTextView.text = info.transitData.routeId
                holder.tripHeadsignTextView.setTextColor(
                    holder.itemView.resources
                        .getColor(R.color.basic_blue, null)
                )
                holder.stopNameTextView.text = info.transitData.stopName
                holder.stopNameTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_blue, null)
                )
                holder.directionTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_blue, null)
                )
            }

            //FIXME some margin issues with bus number and bus route long name
            TransitAgency.EXO_OTHER -> {
                info.transitData as ExoBusData
                holder.itemView.tag = info.transitData
                holder.routeLongNameTextView.text = "Bus: ${info.transitData.routeLongName}"
                holder.routeLongNameTextView.visibility = VISIBLE
                holder.routeLongNameTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_purple, null)
                )
                holder.stopNameTextView.text = info.transitData.stopName
                holder.directionTextView.text = "To ${info.transitData.direction}"
                holder.tripHeadsignTextView.text = info.transitData.routeId
                holder.tripHeadsignTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_purple, null)
                )
                holder.stopNameTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_purple, null)
                )
                holder.directionTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_purple, null)
                )
            }
        }

        holder.checkBoxView.setOnClickListener {
            val parent = recyclerView.parent.parent.parent.parent.parent.parent as ViewGroup
            if ((it as MaterialCheckBox).isChecked) holder.select(parent)
            else holder.unSelect(parent)
            parent.findViewById<MaterialTextView>(R.id.selectedNumsOfFavourites)
                .text = (recyclerView.adapter as FavouritesListElemsAdapter).numSelected.run{
                val deleteItemsWidget = parent.findViewById<LinearLayout>(R.id.removeItemsWidget)
                if (this > 0) {
                    if (deleteItemsWidget.visibility == GONE) deleteItemsWidget.visibility = VISIBLE
                    toString()
                }
                else {
                    deleteItemsWidget.visibility = GONE
                    recyclerView.context.getString(R.string.select_favourites_to_remove)
                }
            }
        }

        /**
         * This onClick either serves to get to the Times activity if not in selection mode, or to
         * select/unselect an item of the recycler view if in selection mode (see onLongClick for
         * more detail on the selection mode)
         **/
        holder.itemView.setOnClickListener {
            //FIXME could remove nullability by setting holder.itemview.tag = "unselected"...
            val parent = WeakReference((recyclerView.parent.parent.parent.parent.parent.parent as ViewGroup))
            if (holder.checkBoxView.isChecked){
                holder.unSelect(parent.get()!!)
                holder.checkBoxView.isChecked = false
            }
            else {
                if (recyclerView.tag != null){
                    recyclerView.tag.let{tag ->
                        when(tag){
                            "selected" -> {
                                holder.select(parent.get()!!)
                                holder.checkBoxView.isChecked = true
                            }
                            "unselected" -> startTimes(holder, it, info)
                        }
                    }
                }
                else startTimes(holder, it, info)
            }

            parent.get()!!.findViewById<MaterialTextView>(R.id.selectedNumsOfFavourites)
                .text = (recyclerView.adapter as FavouritesListElemsAdapter).numSelected.run{
                val deleteItemsWidget = parent.get()!!.findViewById<LinearLayout>(R.id.removeItemsWidget)
                if (this > 0) {
                    if (deleteItemsWidget.visibility == GONE) deleteItemsWidget.visibility = VISIBLE
                    toString()
                }
                else {
                    deleteItemsWidget.visibility = GONE
                    recyclerView.context.getString(R.string.select_favourites_to_remove)
                }
            }
        }

        /** This onLongClick function serves as a selection interface (entering selection mode) for the recycler view items, so that
         * we can perform operations on them. */
        holder.itemView.setOnLongClickListener {
            val parent = WeakReference(recyclerView.parent.parent.parent.parent.parent.parent as ViewGroup)
            /** This next line will attempt to change the appbar to allow deletions */
            ((it.context as MainActivity).findViewById<FragmentContainerView>(R.id.mainFragmentContainer)
                .getFragment() as Home).view?.findViewById<AppBarLayout>(R.id.mainAppBar)
                ?.apply{
                    /** This is the search bar that will disappear in the appBar*/
                    children.elementAt(0).visibility = GONE
                    /** This is the constraint layout having the selection mode */
                    children.elementAt(1).visibility = VISIBLE
                }
            val tmpRecyclerView = it.parent as RecyclerView
            val adapter = tmpRecyclerView.adapter as FavouritesListElemsAdapter
            /** if recycler has never been long clicked/has been backed, check if the view is not selected and select it */
            if (tmpRecyclerView.tag == null || tmpRecyclerView.tag == "unselected"){
                if (!holder.checkBoxView.isChecked){
                    holder.select(parent.get()!!)
                    /** Show the checkboxes for each of the favourite elements */
                    recyclerView.forEach {view ->
                        val viewGroup = (view as ViewGroup)[0] as ViewGroup
                        (viewGroup[0] as MaterialCheckBox).visibility = VISIBLE
                        val constraintLayout = viewGroup[1] as ConstraintLayout
                        setMargins(constraintLayout, 10, 15)
                    }
                    holder.checkBoxView.isChecked = true
                }
                tmpRecyclerView.tag = "selected"
                parent.get()!!.findViewById<MaterialTextView>(R.id.selectedNumsOfFavourites)
                    .text = adapter.numSelected.toString()
                parent.get()!!.findViewById<LinearLayout>(R.id.removeItemsWidget).apply{
                    if (visibility == GONE) visibility = VISIBLE
                }
            }
            true
        }
    }

    /**
     * Update the current time for each favourite
     * Only applies for when the arrival time exists (for some buses, some days do not have any trips */
    fun updateTime(viewGroup : ViewGroup, favouritesBusInfo: FavouriteTransitInfo){
        val container = viewGroup[0] as ViewGroup
        favouritesBusInfo.arrivalTime?.also {
            ((container[1] as ViewGroup)[2] as MaterialTextView).text = getTimeRemaining(favouritesBusInfo.arrivalTime)
            ((container[1] as ViewGroup)[3] as MaterialTextView).text = favouritesBusInfo.arrivalTime.getTimeString()

            //set the color to red if the time remaining is less than 3min 59sec (warning)
            if (favouritesBusInfo.arrivalTime < Time(LocalTime.of(0, 3, 59))) ((container[1] as ViewGroup)[2] as MaterialTextView)
                .setTextColor(viewGroup.resources.getColor(R.color.red, null))
            else ((container[1] as ViewGroup)[3] as MaterialTextView).setTextColor(MaterialColors.getColor(viewGroup, android.R.attr.editTextColor))
        }
        /*
        favouritesBusInfo.arrivalTime?.also {
            ((container[1] as ViewGroup)[2] as MaterialTextView).text = getTimeRemaining(it)
            ((container[1] as ViewGroup)[3] as MaterialTextView).text = favouritesBusInfo.arrivalTime.getTimeString()
            if (it.timeRemaining()?.compareTo(Time(0,3,59)) == -1) ((container[1] as ViewGroup)[2] as MaterialTextView)
                .setTextColor(viewGroup.resources.getColor(R.color.red, null))
            else ((container[1] as ViewGroup)[3] as MaterialTextView).setTextColor(MaterialColors.getColor(viewGroup, android.R.attr.editTextColor))
        }
         */
    }

    private fun getTimeRemaining(arrivalTime: Time?): String {
        if (arrivalTime == null) return "Wtf"
        val remainingTime = arrivalTime.timeRemaining()
        return if (remainingTime != null && remainingTime.hour > 0) "In ${remainingTime.hour} h, ${remainingTime.minute} min"
                else if (remainingTime != null) "In ${remainingTime.minute} min"
            else "Bus has passed??"
    }

    /** This function is used to deselect a container
     *  containing the favourite element in the view group.
     *  Mainly used with configuring the back button callbacks, to deselect every items  */
    fun unSelect(/** Outer view group layout, containing the linear layout (at the moment) for the other components */
                 viewGroup : ViewGroup){
        if (((viewGroup[0] as ViewGroup)[0] as MaterialCheckBox).isChecked){
            ((viewGroup[0] as ViewGroup)[0] as MaterialCheckBox).isChecked = false
            numSelected--
        }
    }

    /**
     * @param viewGroup Outer view group layout, containing the linear layout (at the moment) for
     * the other components.
     **/
    fun select(viewGroup : ViewGroup){
        if (!((viewGroup[0] as ViewGroup)[0] as MaterialCheckBox).isChecked){
            ((viewGroup[0] as ViewGroup)[0] as MaterialCheckBox).isChecked = true
            numSelected++
        }
    }

    /**
     * @param viewGroup Outer view group layout, containing the linear layout (at the moment) for
     * the other components.
     **/
    fun isSelected(viewGroup : ViewGroup) : Boolean{
        return ((viewGroup[0] as ViewGroup)[0] as MaterialCheckBox).isChecked
    }

    private fun startTimes(holder : ViewHolder, view : View, info : FavouriteTransitInfo){
        val intent = Intent(view.context, TimesActivity::class.java)
        intent.putExtra("stopName", holder.stopNameTextView.text as String)
        intent.putExtra(BusExtrasInfo.AGENCY.name, info.agency)
        when (info.agency) {
            TransitAgency.EXO_TRAIN -> {
                info.transitData as TrainData
                //2 below are for DB queries
                intent.putExtra(BusExtrasInfo.ROUTE_ID.name, info.transitData.routeId)
                intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, info.transitData.directionId)
                //for final destination display
                intent.putExtra(BusExtrasInfo.DIRECTION.name, info.transitData.direction)
                intent.putExtra(BusExtrasInfo.TRAIN_NUM.name, info.transitData.trainNum)
            }
            TransitAgency.STM -> {
                info.transitData as StmBusData
                intent.putExtra(BusExtrasInfo.ROUTE_ID.name, info.transitData.routeId)
                intent.putExtra(BusExtrasInfo.DIRECTION.name, info.transitData.direction)
            }
            TransitAgency.EXO_OTHER -> {
                info.transitData as ExoBusData
                intent.putExtra(BusExtrasInfo.ROUTE_ID.name, info.transitData.routeId)
                //intent.putExtra(BusExtrasInfo.DIRECTION.name, info.transitData.direction)
                intent.putExtra(BusExtrasInfo.HEADSIGN.name, info.transitData.headsign)
            }
        }
        holder.itemView.context.startActivity(intent)
        view.clearFocus()
    }

    class ViewHolder(view : View, private val recyclerView: RecyclerView) : RecyclerView.ViewHolder(view){
        var checkBoxView : MaterialCheckBox = view.findViewById(R.id.favourites_check_box)
        /** Will consist of the routeLongName for exo data */
        val tripHeadsignTextView : MaterialTextView = view.findViewById(R.id.favouritesTripheadsignTextView)
        val stopNameTextView : MaterialTextView = view.findViewById(R.id.favouritesStopNameTextView)
        val arrivalTimeTextView : MaterialTextView = view.findViewById(R.id.favouritesBusTimeTextView)
        val timeRemainingTextView : MaterialTextView = view.findViewById(R.id.favouritesBusTimeRemainingTextView)
        val directionTextView : MaterialTextView = view.findViewById(R.id.favouritesDirectionTextView)
        /** Invisible for all except exo buses */
        val routeLongNameTextView: MaterialTextView = view.findViewById(R.id.favouritesExoRouteLongNameTextView)

        fun select(parent : ViewGroup?){
            parent?.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox)?.apply {
                (recyclerView.adapter as FavouritesListElemsAdapter).also {
                    if (it.numSelected == it.itemCount - 1) {
                        if (!isChecked) isChecked = true
                    }
                }
            }
            (recyclerView.adapter as FavouritesListElemsAdapter).numSelected++
        }

        fun unSelect(parent : ViewGroup?){
            parent?.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox)?.apply {
                    if (isChecked) isChecked = false
            }
            (recyclerView.adapter as FavouritesListElemsAdapter).numSelected--
        }

    }
}

/**
 * Set the margins for the left margin for the left most items
 *  and the right margin for the right most items inside the recycler view
 **/
fun setMargins(constraintLayout : ConstraintLayout, left : Int, right : Int){
    for (i in 0..< constraintLayout.size){
        val materialTextView = constraintLayout[i] as MaterialTextView
        if (i == 0 || i == 1 || i == 4){
            (materialTextView.layoutParams as ViewGroup.MarginLayoutParams)
                .setMargins(left, 0, 0, 0)
        }
        else if (i == 2 || i == 3){
            (materialTextView.layoutParams as ViewGroup.MarginLayoutParams)
                .setMargins(0, 0, right, 0)
        }
    }
}