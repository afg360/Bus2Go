package dev.mainhq.bus2go.presentation.main.home.favourites

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnLongClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.TimesActivity
import dev.mainhq.bus2go.domain.entity.ExoFavouriteBusItem
import dev.mainhq.bus2go.domain.entity.ExoFavouriteTrainItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.FavouriteTransitDataWithTime
import dev.mainhq.bus2go.domain.entity.StmFavouriteBusItem
import java.time.LocalTime

//FIXME could make the list thing a bit more efficient and simply change all the times instead
/**
 * @param onClickListener An onClickListener for each item in the adapter. It should either start the
 * Times activity if not in selection mode, or to allow selection/deselection of an item of the
 * recycler view if in selection mode. Should select the checkbox and the thing itself.
 * @param onLongClickListener An onLongClickListener for each item in the adapter.
 **/
class FavouritesListElemsAdapter(
    private var list : List<FavouriteTransitDataWithTime>,
    private val onClickListener: (View, FavouriteTransitData) -> Unit,
    private val onLongClickListener: OnLongClickListener
)
    : RecyclerView.Adapter<FavouritesListElemsAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.favourites_list_elem, parent, false)
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
            val timeRemaining = info.arrivalTime.timeRemaining()
            holder.timeRemainingTextView.text = getTimeRemaining(timeRemaining)
            //if (info.arrivalTime.timeRemaining()?.compareTo(Time(0,3,59)) == -1)
            if (timeRemaining == null || timeRemaining < LocalTime.of(0, 4, 0))
                holder.timeRemainingTextView.setTextColor(holder.itemView.resources.getColor(R.color.red, null))
            else
                holder.timeRemainingTextView.setTextColor(MaterialColors.getColor(holder.itemView, android.R.attr.editTextColor))
        }

        when(info.favouriteTransitData) {
            //FIXME some margin issues with bus number and bus route long name
            is ExoFavouriteBusItem -> {
                holder.itemView.tag = info.favouriteTransitData
                holder.routeLongNameTextView.text = "Bus: ${info.favouriteTransitData.routeLongName}"
                holder.routeLongNameTextView.visibility = VISIBLE
                holder.routeLongNameTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_purple, null)
                )
                holder.stopNameTextView.text = info.favouriteTransitData.stopName
                holder.directionTextView.text = "To ${info.favouriteTransitData.direction}"
                holder.tripHeadsignTextView.text = info.favouriteTransitData.routeId
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

            is ExoFavouriteTrainItem -> {
                holder.routeLongNameTextView.text = "" //clear it out even if gone
                holder.routeLongNameTextView.visibility = GONE
                holder.directionTextView.text = holder.itemView.context
                    .getString(R.string.train_to, info.favouriteTransitData.trainNum.toString(), info.favouriteTransitData.direction)
                holder.itemView.tag = info.favouriteTransitData
                holder.stopNameTextView.text = info.favouriteTransitData.stopName
                holder.tripHeadsignTextView.text = info.favouriteTransitData.routeName
                //FIXME for testing purposes, the below is added
                holder.tripHeadsignTextView.tag = info.favouriteTransitData.direction
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

            is StmFavouriteBusItem -> {
                holder.routeLongNameTextView.text = "" //clear it out even if gone
                holder.routeLongNameTextView.visibility = GONE
                holder.itemView.tag = info.favouriteTransitData
                holder.directionTextView.text = "To ${info.favouriteTransitData.lastStop}"
                holder.tripHeadsignTextView.text = info.favouriteTransitData.routeId
                holder.tripHeadsignTextView.setTextColor(
                    holder.itemView.resources
                        .getColor(R.color.basic_blue, null)
                )
                holder.stopNameTextView.text = info.favouriteTransitData.stopName
                holder.stopNameTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_blue, null)
                )
                holder.directionTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_blue, null)
                )
            }
        }

        holder.checkBoxView.setOnClickListener{
            holder.checkBoxView.isChecked = !holder.checkBoxView.isChecked
        }

        /**
         * This onClick either
         **/
        holder.itemView.setOnClickListener {
            onClickListener(it, info.favouriteTransitData)
        }

        holder.itemView.setOnLongClickListener(onLongClickListener)
        holder.itemView.setOnLongClickListener {
            val tmpRecyclerView = it.parent as RecyclerView
            /** if recycler has never been long clicked/has been backed, check if the view is not selected and select it */
            if (tmpRecyclerView.tag == null || tmpRecyclerView.tag == "unselected"){
                if (!holder.checkBoxView.isChecked){
                    /*
                    /** Show the checkboxes for each of the favourite elements */
                    recyclerView.forEach {view ->
                        val viewGroup = (view as ViewGroup)[0] as ViewGroup
                        (viewGroup[0] as MaterialCheckBox).visibility = VISIBLE
                        val constraintLayout = viewGroup[1] as ConstraintLayout
                        setMargins(constraintLayout, 10, 15)
                    }
                     */
                    holder.checkBoxView.isChecked = true
                }
            }
            true
        }
    }

    fun updateTime(list: List<FavouriteTransitDataWithTime>){
        this.list = list
        notifyItemRangeChanged(0, itemCount)
    }


    private fun getTimeRemaining(remainingTime: LocalTime?): String {
        //if (arrivalTime == null) return "Wtf"
        //val remainingTime = arrivalTime.timeRemaining()
        return if (remainingTime != null && remainingTime.hour > 0) "In ${remainingTime.hour} h, ${remainingTime.minute} min"
                else if (remainingTime != null) "In ${remainingTime.minute} min"
            else "Bus has passed??"
    }



    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var checkBoxView : MaterialCheckBox = view.findViewById(R.id.favourites_check_box)
        /** Will consist of the routeLongName for exo data */
        val tripHeadsignTextView : MaterialTextView = view.findViewById(R.id.favouritesTripheadsignTextView)
        val stopNameTextView : MaterialTextView = view.findViewById(R.id.favouritesStopNameTextView)
        val arrivalTimeTextView : MaterialTextView = view.findViewById(R.id.favouritesBusTimeTextView)
        val timeRemainingTextView : MaterialTextView = view.findViewById(R.id.favouritesBusTimeRemainingTextView)
        val directionTextView : MaterialTextView = view.findViewById(R.id.favouritesDirectionTextView)
        /** Invisible for all except exo buses */
        val routeLongNameTextView: MaterialTextView = view.findViewById(R.id.favouritesExoRouteLongNameTextView)
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