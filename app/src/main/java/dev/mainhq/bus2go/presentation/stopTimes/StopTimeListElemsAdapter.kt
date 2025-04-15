package dev.mainhq.bus2go.presentation.stopTimes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.mainhq.bus2go.R
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.presentation.main.home.favourites.Urgency

//TODO
//could add view/onTouchListener to handle touch holding, etc.
class StopTimeListElemsAdapter(
	private var timeData: List<StopTimesDisplayModel>,
	private val fromAlarmCreation : Boolean
) : RecyclerView.Adapter<StopTimeListElemsAdapter.ViewHolder>() {

    companion object {
        const val TIME_DISPLAY_PAYLOAD = "TIME_DISPLAY_PAYLOAD"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.elem_time_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>){
        if (payloads.isEmpty())
            onBindViewHolder(holder, position)
        else if (payloads[0] == TIME_DISPLAY_PAYLOAD) {
            displayTimeRemaining(position, timeData[position], holder)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stopTimesDisplayModel = timeData[position]
        displayTimeRemaining(position, stopTimesDisplayModel, holder)
        holder.timeTextView.text = stopTimesDisplayModel.arrivalTime.getTimeString()
    }

    fun update(timeData: List<StopTimesDisplayModel>){
        //the list may shrink or may be refilled (i.e. "new day")
        if (this.timeData.size != timeData.size){
            this.timeData = timeData
            notifyItemRangeChanged(0, timeData.size)
        }
        else {
            this.timeData = timeData
            notifyItemRangeChanged(0, timeData.size, TIME_DISPLAY_PAYLOAD)
        }
    }

    private fun displayTimeRemaining(position: Int, stopTimesDisplayModel: StopTimesDisplayModel, holder: ViewHolder){
        if (position < 3 && !fromAlarmCreation){
            if (stopTimesDisplayModel.timeLeftTextDisplay.isEmpty())
                holder.timeLeftTextView.text =
                    holder.itemView.context.getString(R.string.in_more_than_an_hour)
            else holder.timeLeftTextView.text =
                holder.itemView.context.getString(R.string.in_min, stopTimesDisplayModel.timeLeftTextDisplay)

            when (stopTimesDisplayModel.urgency){
                Urgency.IMMINENT -> holder.timeLeftTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.red, null)
                )
                Urgency.SOON -> holder.timeLeftTextView.setTextColor(
                    holder.itemView.resources.getColor(R.color.yellow, null)
                )
                Urgency.DISTANT -> holder.timeLeftTextView.setTextColor(
                    MaterialColors.getColor(holder.itemView, android.R.attr.editTextColor)
                )
            }
        }
        /* NOTE a VIEWHOLDER can be recycled, which would mean that all of its attributes would be reused!!*/
        else holder.timeLeftTextView.text = ""
    }

    override fun getItemCount(): Int {
        return timeData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val timeTextView: MaterialTextView = view.findViewById(R.id.time)
        var timeLeftTextView: MaterialTextView = view.findViewById(R.id.time_left)
    }
}
