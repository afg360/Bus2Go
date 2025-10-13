package dev.mainhq.bus2go.presentation.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo

class BusListElemsAdapter(
    private var busData: List<RouteInfo>,
    private val onClickListener: (RouteInfo) -> Unit
) : RecyclerView.Adapter<BusListElemsAdapter.ViewHolder>() {
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
        holder.itemView.setOnClickListener{ onClickListener(data) }

        val color = when(data){
            is ExoBusRouteInfo -> R.color.basic_purple
            is ExoTrainRouteInfo -> R.color.orange
            is StmBusRouteInfo -> {
                if (data.routeId.toInt() in 400..499){
                    R.color.basic_green
                }
                else {
                    R.color.basic_blue
                }
            }
        }

        holder.busNumView.setTextColor(holder.itemView.resources.getColor(color, null))
        holder.busDirView.setTextColor(holder.itemView.resources.getColor(color,  null))
    }

	fun updateData(busData: List<RouteInfo>){
        this.busData = busData
        notifyDataSetChanged()
        //notifyItemRangeChanged(0, this.busData.size) //TODO could compare the list itself, or apparently use DiffUtil
    }

    override fun getItemCount(): Int {
        return busData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busDirView: MaterialTextView = view.findViewById(R.id.busDir)
        val busNumView: MaterialTextView = view.findViewById(R.id.busNum)
    }
}
