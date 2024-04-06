package dev.mainhq.schedules.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
import dev.mainhq.schedules.database.dao.StopInfo

//TODO
//could add view/ontouchlistener to handle touch holding, etc.
//may need to use a recycler view, but implement a base adapter instead...?
class TimeListElemsAdapter(private val timeData: List<StopInfo>)//todo List<Pair<String,String>>
    : RecyclerView.Adapter<TimeListElemsAdapter.ViewHolder>() {

    private interface Listener {
        fun onClickListener()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.time_list_elem, parent, false), null
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = timeData[position]
        holder.stopNameView.text = data.stopName
        holder.stopNameView.tag = data.stopCode
    }

    override fun getItemCount(): Int {
        return timeData.size
    }

    class ViewHolder(view: View, stopId : Int?) : RecyclerView.ViewHolder(view) {
        val stopNameView: TextView

        init {
            stopNameView = view.findViewById(R.id.stop)
        }
    }
}
