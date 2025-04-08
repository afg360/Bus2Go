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
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.TransitDataWithTime
import dev.mainhq.bus2go.domain.entity.StmBusItem
import java.time.LocalTime

//FIXME could make the list thing a bit more efficient and simply change all the times instead
/**
 * @param onClickListener An onClickListener for each item in the adapter. It should either start the
 * Times activity if not in selection mode, or to allow selection/deselection of an item of the
 * recycler view if in selection mode. Should select the checkbox and the thing itself.
 * @param onLongClickListener An onLongClickListener for each item in the adapter.
 **/
class FavouritesListElemsAdapter(
    private var list : List<FavouriteTransitDataWithTimeAndSelection>,
    private val onClickListener: (View, TransitData) -> Unit,
    private val onLongClickListener: (View, TransitData) -> Boolean,
)
    : RecyclerView.Adapter<FavouritesListElemsAdapter.ViewHolder>(){

    private var selectedMode = false

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

        holder.arrivalTimeTextView.text = info.transitDataWithTime.arrivalTime?.getTimeString()
        if (info.transitDataWithTime.arrivalTime == null){
            holder.timeRemainingTextView.text =
                holder.itemView.context.getString(R.string.none_for_the_rest_of_the_day)
        }
        else{
            val timeRemaining = info.transitDataWithTime.arrivalTime.timeRemaining()
            holder.timeRemainingTextView.text = getTimeRemaining(timeRemaining)
            //if (info.arrivalTime.timeRemaining()?.compareTo(Time(0,3,59)) == -1)
            if (timeRemaining == null || timeRemaining < LocalTime.of(0, 4, 0))
                holder.timeRemainingTextView.setTextColor(holder.itemView.resources.getColor(R.color.red, null))
            else
                holder.timeRemainingTextView.setTextColor(MaterialColors.getColor(holder.itemView, android.R.attr.editTextColor))
        }

        when(info.transitDataWithTime.favouriteTransitData) {
            //FIXME some margin issues with bus number and bus route long name
            is ExoBusItem -> {
                holder.itemView.tag = info.transitDataWithTime.favouriteTransitData
                holder.routeLongNameTextView.text = "Bus: ${info.transitDataWithTime.favouriteTransitData.routeLongName}"
                holder.routeLongNameTextView.visibility = VISIBLE
                holder.routeLongNameTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_purple, null)
                )
                holder.stopNameTextView.text = info.transitDataWithTime.favouriteTransitData.stopName
                holder.directionTextView.text = "To ${info.transitDataWithTime.favouriteTransitData.direction}"
                holder.tripHeadsignTextView.text = info.transitDataWithTime.favouriteTransitData.routeId
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

            is ExoTrainItem -> {
                holder.routeLongNameTextView.text = "" //clear it out even if gone
                holder.routeLongNameTextView.visibility = GONE
                holder.directionTextView.text = holder.itemView.context
                    .getString(R.string.train_to, info.transitDataWithTime.favouriteTransitData.trainNum.toString(), info.transitDataWithTime.favouriteTransitData.direction)
                holder.itemView.tag = info.transitDataWithTime.favouriteTransitData
                holder.stopNameTextView.text = info.transitDataWithTime.favouriteTransitData.stopName
                holder.tripHeadsignTextView.text = info.transitDataWithTime.favouriteTransitData.routeName
                //FIXME for testing purposes, the below is added
                holder.tripHeadsignTextView.tag = info.transitDataWithTime.favouriteTransitData.direction
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

            is StmBusItem -> {
                holder.routeLongNameTextView.text = "" //clear it out even if gone
                holder.routeLongNameTextView.visibility = GONE
                holder.itemView.tag = info.transitDataWithTime.favouriteTransitData
                holder.directionTextView.text = "To ${info.transitDataWithTime.favouriteTransitData.lastStop}"
                holder.tripHeadsignTextView.text = info.transitDataWithTime.favouriteTransitData.routeId
                holder.tripHeadsignTextView.setTextColor(
                    holder.itemView.resources
                        .getColor(R.color.basic_blue, null)
                )
                holder.stopNameTextView.text = info.transitDataWithTime.favouriteTransitData.stopName
                holder.stopNameTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_blue, null)
                )
                holder.directionTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.basic_blue, null)
                )
            }
        }

        if (selectedMode) holder.checkBoxView.visibility = VISIBLE
        else holder.checkBoxView.visibility = GONE

        holder.checkBoxView.isChecked = info.isSelected

        holder.checkBoxView.setOnClickListener{
            holder.checkBoxView.isChecked = !holder.checkBoxView.isChecked
            onClickListener(it, info.transitDataWithTime.favouriteTransitData)
        }

        holder.itemView.setOnClickListener { onClickListener(it, info.transitDataWithTime.favouriteTransitData) }

        holder.itemView.setOnLongClickListener{
            onLongClickListener(it, info.transitDataWithTime.favouriteTransitData)
        }
    }

    fun updateTime(list: List<FavouriteTransitDataWithTimeAndSelection>){
        this.list = list
        notifyItemRangeChanged(0, this.list.size)
    }

    fun selectForRemoval(item: FavouriteTransitDataWithTimeAndSelection){
        val list = this.list.toMutableList()
        val index = list.indexOfFirst { it.transitDataWithTime.favouriteTransitData == item.transitDataWithTime.favouriteTransitData }
        val updatedItem = list[index].copy(isSelected = !list[index].isSelected)
        list[index] = updatedItem
        notifyItemChanged(index)
    }

    /** Toggles what mode we are in */
    fun updateSelectionMode(){
        this.selectedMode = !this.selectedMode
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