package dev.mainhq.bus2go.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.preferences.BusInfo

class AlarmDialogListElemsAdapter(private val list : List<BusInfo>, private val recyclerView: RecyclerView)
    : RecyclerView.Adapter<AlarmDialogListElemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val alarmDialogBusNum : MaterialTextView = view.findViewById(R.id.alarmDialogBusNum)
        //val alarmDialogBusLine : MaterialTextView = view.findViewById(R.id.alarmDialogBusLine)
        val alarmDialogBusStop : MaterialTextView = view.findViewById(R.id.alarmDialogBusStop)
        val radioButton : MaterialRadioButton = view.findViewById(R.id.radio_button_1)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.elem_list_add_alarm_dialog, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.alarmDialogBusNum.text = data.tripHeadsign
        //holder.alarmDialogBusLine.text = data.busLine
        holder.alarmDialogBusStop.text = data.stopName
        holder.itemView.setOnClickListener {
            val radioButton : MaterialRadioButton = it.findViewById(R.id.radio_button_1)
            if (!radioButton.isChecked) {
                recyclerView.forEach {view ->
                    val viewGroup = view as ViewGroup
                    viewGroup.findViewById<MaterialRadioButton>(R.id.radio_button_1).isChecked =
                        false
                }
                radioButton.isChecked = true
            }
        }
    }
}