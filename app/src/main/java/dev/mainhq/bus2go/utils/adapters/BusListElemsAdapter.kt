package dev.mainhq.bus2go.utils.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.ChooseDirection
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.database.stm_data.dao.BusRouteInfo

//TODO
//could add view/ontouchlistener to handle touch holding, etc.
//may need to use a recycler view, but implement a base adapter instead...?
class BusListElemsAdapter(private val busData: List<BusRouteInfo>) :
    RecyclerView.Adapter<BusListElemsAdapter.ViewHolder>() {
    //when doing bus num >= 400, then color = green
    // if  >= 300, then color = black
    // else blue
    // if 700, then green (but same as 400)

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
        holder.onClick(holder.itemView)
    }

    override fun getItemCount(): Int {
        return busData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), OnClickListener {
        val busDirView: MaterialTextView
        val busNumView: MaterialTextView
        init {
            busDirView = view.findViewById(R.id.busDir)
            busNumView = view.findViewById(R.id.busNum)
        }

        override fun onClick(view: View?) {
            view?.setOnClickListener {
                val layout = it as ConstraintLayout
                val intent = Intent(it.context, ChooseDirection::class.java)
                intent.putExtra(
                    "busName",
                    (layout.getChildAt(0) as TextView).text.toString()
                )
                intent.putExtra(
                    "busNum",
                    (layout.getChildAt(1) as TextView).text.toString()
                )
                it.context.startActivity(intent)
                it.clearFocus()
            }
        }
    }
}
