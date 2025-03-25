package dev.mainhq.bus2go.presentation.main.home

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.ChooseDirection
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.utils.BusExtrasInfo

class BusListElemsAdapter(private var busData: List<RouteInfo>) :
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
            intent.putExtra(BusExtrasInfo.ROUTE_INFO.name, data)
            //intent.putExtra(BusExtrasInfo.ROUTE_NAME.name, data.routeName)
            //intent.putExtra(BusExtrasInfo.ROUTE_ID.name, holder.busNumView.text.toString())
            //if (data::class.java == ExoTrainRouteInfo::class.java){
            //    val train = data as ExoTrainRouteInfo
            //    intent.putExtra(BusExtrasInfo.TRAIN_NUM.name, train.trainNum)
            //}
            //intent.putExtra(BusExtrasInfo.AGENCY.name, data.transitAgency)
            it.context.startActivity(intent)
            it.clearFocus()
        }
        
        when(data){
            is ExoBusRouteInfo -> {
                holder.busNumView.setTextColor(holder.itemView.resources .getColor(R.color.basic_purple, null))
                holder.busDirView.setTextColor(holder.itemView.resources .getColor(R.color.basic_purple, null))
            }
            is ExoTrainRouteInfo -> {
                holder.busNumView.setTextColor(holder.itemView.resources .getColor(R.color.orange, null))
                holder.busDirView.setTextColor(holder.itemView.resources .getColor(R.color.orange, null))
            }
            is StmBusRouteInfo -> {
                holder.busNumView.setTextColor(holder.itemView.resources .getColor(R.color.basic_blue, null))
                holder.busDirView.setTextColor(holder.itemView.resources .getColor(R.color.basic_blue, null))
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
	fun updateData(busData: List<RouteInfo>){
        this.busData = busData
        notifyDataSetChanged() //TODO could compare the list itself, or apparently use DiffUtil
    }

    override fun getItemCount(): Int {
        return busData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busDirView: MaterialTextView = view.findViewById(R.id.busDir)
        val busNumView: MaterialTextView = view.findViewById(R.id.busNum)
    }
}
