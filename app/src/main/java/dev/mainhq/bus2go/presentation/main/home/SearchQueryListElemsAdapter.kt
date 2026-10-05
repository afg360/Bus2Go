package dev.mainhq.bus2go.presentation.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.R as MaterialRes
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.utils.getMaterialColor

class SearchQueryListElemsAdapter(
    private var busData: List<RouteInfo>,
    private val onClickListener: (RouteInfo) -> Unit
) : RecyclerView.Adapter<SearchQueryListElemsAdapter.ViewHolder>() {
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
            is ExoBusRouteInfo -> R.attr.colorCustomColor1
            is ExoTrainRouteInfo -> R.attr.colorCustomColor2
            is StmBusRouteInfo -> {
                if (data.routeId.toInt() in 400..499){
                    R.attr.colorCustomColor3
                }
                else {
                    MaterialRes.attr.colorPrimary
                }
            }
        }

        holder.busNumView.setTextColor(holder.itemView.getMaterialColor(color))
        holder.busDirView.setTextColor(holder.itemView.getMaterialColor(color))
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
