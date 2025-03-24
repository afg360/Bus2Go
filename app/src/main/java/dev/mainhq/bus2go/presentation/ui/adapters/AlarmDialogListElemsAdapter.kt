package dev.mainhq.bus2go.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.presentation.ui.fragments.alarms.AlarmCreationDialogBottomNavBar
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoBusData

//FIXME may need to get rid of these references
class AlarmDialogListElemsAdapter(private val list : List<ExoBusData>, private val recyclerView: RecyclerView,
                                  private val bottomNavBar: AlarmCreationDialogBottomNavBar
)
    : RecyclerView.Adapter<AlarmDialogListElemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val alarmDialogBusNum : MaterialTextView = view.findViewById(R.id.alarmDialogBusNum)
        //val alarmDialogBusLine : MaterialTextView = view.findViewById(R.id.alarmDialogBusLine)
        val alarmDialogBusStop : MaterialTextView = view.findViewById(R.id.alarmDialogBusStop)
        val radioButton : MaterialRadioButton = view.findViewById(R.id.addAlarmBusChoiceRadioButton)

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
        holder.alarmDialogBusNum.text = data.routeId
        //holder.alarmDialogBusLine.text = data.busLine
        holder.alarmDialogBusStop.text = data.stopName
        holder.itemView.setOnClickListener {
            val radioButton : MaterialRadioButton = it.findViewById(R.id.addAlarmBusChoiceRadioButton)
            if (!radioButton.isChecked) {
                recyclerView.forEach {view ->
                    val viewGroup = view as ViewGroup
                    viewGroup.findViewById<MaterialRadioButton>(R.id.addAlarmBusChoiceRadioButton)
                        .isChecked = false
                }
                radioButton.isChecked = true
            }
            bottomNavBar.activateAcceptAlarmButton()
        }
    }
}