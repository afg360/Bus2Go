package dev.mainhq.bus2go.presentation.stopTimes

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.R
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import dev.mainhq.bus2go.domain.entity.Time
import java.time.LocalDateTime

//TODO
//could add view/ontouchlistener to handle touch holding, etc.
//may need to use a recycler view, but implement a base adapter instead...?
class StopTimeListElemsAdapter(
	private var timeData: List<Time>,
	private val fromAlarmCreation : Boolean
) : RecyclerView.Adapter<StopTimeListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.elem_time_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val time = timeData[position]
        holder.timeTextView.text = time.getTimeString()
        if (position < 3 && !fromAlarmCreation){
            //TODO may need to use a LocalDateTime here...
            val curTime = Time(LocalDateTime.now())
            //Duration.between =! arg1 - arg2, == arg2 - arg1
            val remainingTime = time - curTime
            //val remainingTime = time.minusHours(curTime.hour).minusMinutes(curTime.minute).minusSeconds(curTime.second)
            //if (remainingTime != null) {
            if (remainingTime != null) {
                if (remainingTime.hour == 0) holder.timeLeftTextView.text =
                    holder.itemView.context.getString(R.string.in_min, remainingTime.minute.toString())
                else holder.timeLeftTextView.text = holder.itemView.context.getString(R.string.in_more_than_an_hour)//todo
            }
            else{
                holder.timeLeftTextView.text = "Passed bus???"
            }
        }
        /** NOTE a VIEWHOLDER can be recycled, which would mean that all of its attributes would be reused!!*/
        else holder.timeLeftTextView.text = ""

        holder.itemView.setOnClickListener {
            if (fromAlarmCreation) {
                val resultIntent = Intent()
                resultIntent.putExtra(ExtrasTagNames.TIME, time)
                (it.context as Activity).apply {
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    fun update(timeData: List<Time>){
        this.timeData = timeData
        notifyItemRangeChanged(0, timeData.size)
    }

    override fun getItemCount(): Int {
        return timeData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val timeTextView: MaterialTextView = view.findViewById(R.id.time)
        var timeLeftTextView: MaterialTextView = view.findViewById(R.id.time_left)
    }
}
