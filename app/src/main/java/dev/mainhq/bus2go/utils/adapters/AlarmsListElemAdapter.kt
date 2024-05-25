package dev.mainhq.bus2go.utils.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.preferences.Alarm

class AlarmsListElemAdapter(private val list : List<Alarm>)
    : RecyclerView.Adapter<AlarmsListElemAdapter.ViewHolder>() {

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        private val materialSwitch : MaterialSwitch = view.findViewById(R.id.alarmSwitch)
        val alarmTitle : MaterialSwitch = view.findViewById(R.id.alarmTitle)
        val alarmBusInfo : MaterialSwitch = view.findViewById(R.id.alarmBusInfo)
        val alarmTimeBefore : MaterialSwitch = view.findViewById(R.id.alarmTimeBefore)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.alarmTitle.text = data.title
        holder.alarmBusInfo.text = data.busInfo.toString()
        holder.alarmTimeBefore.text = data.timeBefore.toString()
        //holder.itemView.setOnClickListener {
        //
        //}
    }
}