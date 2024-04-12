package dev.mainhq.schedules.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
import dev.mainhq.schedules.preferences.BusInfo

class FavouritesListElemsAdapter(private val list : List<BusInfo>)
    : RecyclerView.Adapter<FavouritesListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.favourites_list_elem, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = list[position]
        holder.tripHeadsignTextView.text = info.tripHeadsign
        holder.busNumTextView.text = info.busLine
    }


    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val tripHeadsignTextView : TextView
        val busNumTextView : TextView
        init{
            tripHeadsignTextView = view.findViewById(R.id.favouritesTripheadsignTextView)
            busNumTextView = view.findViewById(R.id.favouritesBusNumTextView)
        }
    }
}