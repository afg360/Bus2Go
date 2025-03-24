package dev.mainhq.bus2go.presentation.ui.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.R
import android.icu.util.Calendar
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.utils.Time
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period

//TODO
//could add view/ontouchlistener to handle touch holding, etc.
//may need to use a recycler view, but implement a base adapter instead...?
class TimeListElemsAdapter(private val timeData: List<Time>,
                           private val fromAlarmCreation : Boolean)
    : RecyclerView.Adapter<TimeListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.elem_time_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val time = timeData[position]
        holder.timeTextView.text = time.getTimeString()
        if (position == 0 && !fromAlarmCreation){
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
                resultIntent.putExtra("TIME", time)
                (it.context as Activity).apply {
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return timeData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val timeTextView: MaterialTextView
        var timeLeftTextView: MaterialTextView
        init {
            timeTextView = view.findViewById(R.id.time)
            timeLeftTextView = view.findViewById(R.id.time_left)
        }
    }
}
