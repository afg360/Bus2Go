package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.ChooseDirection
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.utils.TransitInfo

//TODO
//could add view/ontouchlistener to handle touch holding, etc.
//may need to use a recycler view, but implement a base adapter instead...?
class BusListElemsAdapter(private val busData: List<TransitInfo>) :
    RecyclerView.Adapter<BusListElemsAdapter.ViewHolder>() {
    //when doing bus num >= 400, then color = green
    // if  >= 300, then color = black
    // else blue
    // if 700, then green (but same as 400)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.elem_bus_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = busData[position]
        holder.busNumView.text = data.routeId
        holder.busDirView.text = data.routeName
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, ChooseDirection::class.java)
            intent.putExtra(BusExtrasInfo.ROUTE_NAME.name, data.routeName)
            intent.putExtra(BusExtrasInfo.ROUTE_ID.name, holder.busNumView.text.toString())
            data.trainNum?.apply {
                intent.putExtra(BusExtrasInfo.TRAIN_NUM.name, this)
            }
            intent.putExtra(BusExtrasInfo.AGENCY.name, data.transitAgency)
            it.context.startActivity(intent)
            it.clearFocus()
        }
        
        when(data.transitAgency){
            TransitAgency.STM -> {
                holder.busNumView.setTextColor(holder.itemView.resources .getColor(R.color.basic_blue, null))
                holder.busDirView.setTextColor(holder.itemView.resources .getColor(R.color.basic_blue, null))
            }
            TransitAgency.EXO_TRAIN -> {
                holder.busNumView.setTextColor(holder.itemView.resources .getColor(R.color.orange, null))
                holder.busDirView.setTextColor(holder.itemView.resources .getColor(R.color.orange, null))
            }
            TransitAgency.EXO_OTHER -> {
                holder.busNumView.setTextColor(holder.itemView.resources .getColor(R.color.basic_purple, null))
                holder.busDirView.setTextColor(holder.itemView.resources .getColor(R.color.basic_purple, null))
            }
        }
    }

    override fun getItemCount(): Int {
        return busData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busDirView: MaterialTextView
        val busNumView: MaterialTextView
        init {
            busDirView = view.findViewById(R.id.busDir)
            busNumView = view.findViewById(R.id.busNum)
        }
    }
}
