package dev.mainhq.bus2go.presentation.main.home.favourites

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.TransitData

//FIXME could make the list thing a bit more efficient and simply change all the times instead
/**
 * @param onClickListener An onClickListener for each item in the adapter. It should either start the
 * Times activity if not in selection mode, or to allow selection/deselection of an item of the
 * recycler view if in selection mode. Should select the checkbox and the thing itself.
 * @param onLongClickListener An onLongClickListener for each item in the adapter.
 **/
class FavouritesListElemsAdapter(
    private var list : List<FavouritesDisplayModel>,
    private val onClickListener: (View, TransitData) -> Unit,
    private val onLongClickListener: (View, TransitData) -> Boolean,
    private var toRemoveList: List<TransitData>
)
    : RecyclerView.Adapter<FavouritesListElemsAdapter.ViewHolder>(){

    companion object {
        private const val CHECKBOXES_PAYLOAD = "CHECKBOXES"
        private const val TIME_PAYLOAD = "TIME"
    }

    private var selectedMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.favourites_list_elem, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    //allows us to not refresh the whole recyclerView...
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            onBindViewHolder(holder, position)
        else if (payloads[0] == CHECKBOXES_PAYLOAD) {
            holder.checkBoxView.isChecked = list[position].isToRemove(toRemoveList)
        }
        else if (payloads[0] == TIME_PAYLOAD) {
            setTimeLeft(holder, position)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = list[position]
        setTimeLeft(holder, position)

        holder.stopNameTextView.text = info.stopNameText
        if (info.toTruncate){
            holder.directionTextView.text = "${info.directionText.substring(0, FavouritesDisplayModel.DIRECTION_STR_LIMIT)}..."
            holder.directionTextView.setOnClickListener {
                if (holder.directionTextView.text.contains("..."))
                    holder.directionTextView.text = info.directionText
                else holder.directionTextView.text = "${info.directionText.substring(0, FavouritesDisplayModel.DIRECTION_STR_LIMIT)}..."
            }
        }
        else holder.directionTextView.text = info.directionText
        holder.tripHeadsignTextView.text = info.tripHeadsignText

        //val drawable = holder.itemView.resources.getDrawable(R.drawable.favourites_tripheadsign_background, null)
        //drawable.setTint(holder.itemView.resources.getColor(info.dataDisplayColor, null))
        //holder.tripHeadsignTextView.setBackgroundDrawable(drawable)
        holder.tripHeadsignTextView.setTextColor(holder.itemView.resources.getColor(info.dataDisplayColor, null))

        if (selectedMode) holder.checkBoxView.visibility = VISIBLE
        else holder.checkBoxView.visibility = GONE

        holder.checkBoxView.isChecked = toRemoveList.contains(info.favouriteTransitData)

        holder.itemView.setOnClickListener { onClickListener(it, info.favouriteTransitData) }
        //holder.checkBoxView.setOnClickListener { onClickListener(holder.itemView, info.favouriteTransitData) }

        holder.itemView.setOnLongClickListener{
            onLongClickListener(it, info.favouriteTransitData)
        }
    }

    private fun setTimeLeft(holder: ViewHolder, position: Int){
        val info = list[position]
        holder.timeRemainingTextView.text = info.timeRemainingText
        if (info.arrivalTimeText == null) {
            holder.timeRemainingTextView.textSize = 20F
            holder.timeRemainingTextView.setTextColor(holder.itemView.resources.getColor(R.color.light_grey, null))
            holder.arrivalTimeTextView.visibility = GONE
        }
        else {
            holder.timeRemainingTextView.textSize = 30F
            holder.arrivalTimeTextView.text = info.arrivalTimeText
            holder.arrivalTimeTextView.visibility = VISIBLE
            when (info.urgency){
                Urgency.IMMINENT -> holder.timeRemainingTextView.setTextColor(holder.itemView.resources.getColor(R.color.red, null))
                Urgency.SOON -> holder.timeRemainingTextView.setTextColor(holder.itemView.resources.getColor(R.color.yellow, null))
                Urgency.DISTANT -> holder.timeRemainingTextView.setTextColor(MaterialColors.getColor(holder.itemView, android.R.attr.editTextColor))
            }
        }
    }

    fun updateTime(list: List<FavouritesDisplayModel>){
        this.list = list
        notifyItemRangeChanged(0, this.list.size, TIME_PAYLOAD)
    }

    fun toggleForRemoval(items: List<TransitData>){
        this.toRemoveList = items
        notifyItemRangeChanged(0, this.list.size, CHECKBOXES_PAYLOAD)
    }

    fun removeSelected(){
        list = list.toMutableList().filter { !it.isToRemove(toRemoveList) }
        toRemoveList = emptyList()
        notifyDataSetChanged()
    }

    /** Toggles what mode we are in */
    fun updateSelectionMode(){
        this.selectedMode = !this.selectedMode
        notifyItemRangeChanged(0, this.list.size)
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
        //val routeLongNameTextView: MaterialTextView = view.findViewById(R.id.favouritesExoRouteLongNameTextView)
    }
}