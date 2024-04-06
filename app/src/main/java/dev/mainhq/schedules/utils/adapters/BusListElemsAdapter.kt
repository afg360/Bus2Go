package dev.mainhq.schedules.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
import dev.mainhq.schedules.database.dao.BusRouteInfo

//TODO
//could add view/ontouchlistener to handle touch holding, etc.
//may need to use a recycler view, but implement a base adapter instead...?
class BusListElemsAdapter(private val busData: List<BusRouteInfo>) :
    RecyclerView.Adapter<BusListElemsAdapter.ViewHolder>() {
    //when doing bus num >= 400, then color = green
    // if  >= 300, then color = black
    // else blue
    // if 700, then green (but same as 400)
    private interface Listener {
        fun onClickListener()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.bus_list_elem, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = busData[position]
        holder.busNumView.text = data.routeId.toString()
        holder.busDirView.text = data.routeName
    }

    override fun getItemCount(): Int {
        return busData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busDirView: TextView
        val busNumView: TextView

        init {
            busDirView = view.findViewById(R.id.busDir)
            busNumView = view.findViewById(R.id.busNum)
        }
    }
}
