package dev.mainhq.schedules.utils.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.scaleMatrix
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
import android.icu.util.Calendar
import dev.mainhq.schedules.utils.Time

//TODO
//could add view/ontouchlistener to handle touch holding, etc.
//may need to use a recycler view, but implement a base adapter instead...?
class TimeListElemsAdapter(private val timeData: List<Time>)
    : RecyclerView.Adapter<TimeListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.time_list_elem, parent, false)
        )
    }

    //todo for now ignore harcoded text warnings
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val time : Time = timeData[position]
        holder.timeTextView.text = time.toString()
        //todo refresh every 5-10 secs
        //todo perhaps do that where the recycler is made directly instead
        if (position == 0){
            val curTime = Time(Calendar.getInstance())
            val remainingTime = time.subtract(curTime)
            if (remainingTime != null) {
                if (remainingTime.hour == 0) holder.timeLeftTextView.text = "In ${remainingTime.min} min"
                else holder.timeLeftTextView.text = "TODO"
            }
        }

        holder.onClick(holder.itemView)
    }

    override fun getItemCount(): Int {
        return timeData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), OnClickListener{
        val timeTextView: TextView
        var timeLeftTextView: TextView
        init {
            timeTextView = view.findViewById(R.id.time)
            timeLeftTextView = view.findViewById(R.id.time_left)
        }

        override fun onClick(view: View?) {
            view?.setOnClickListener{
                Toast.makeText(it.context, "You touched me!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
