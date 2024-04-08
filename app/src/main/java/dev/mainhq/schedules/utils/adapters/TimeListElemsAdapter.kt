package dev.mainhq.schedules.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val time : Time = timeData[position]
        holder.timeTextView.text = time.toString()
        holder.onClick(holder.itemView)
    }

    override fun getItemCount(): Int {
        return timeData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), OnClickListener{
        val timeTextView: TextView

        init {
            timeTextView = view.findViewById(R.id.time)
        }

        override fun onClick(view: View?) {
            view?.setOnClickListener{
                Toast.makeText(it.context, "You touched me!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
