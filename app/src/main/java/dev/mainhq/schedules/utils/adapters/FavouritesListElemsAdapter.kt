package dev.mainhq.schedules.utils.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
import dev.mainhq.schedules.fragments.FavouriteBusInfo

class FavouritesListElemsAdapter(private val list : List<FavouriteBusInfo>)
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
        holder.tripHeadsignTextView.text = info.busInfo.tripHeadsign
        holder.stopNameTextView.text = info.busInfo.stopName
        holder.arrivalTimeTextView.text = info.arrivalTime.toString()
        holder.onLongClick(holder.itemView)
        //holder.itemView.seton
        holder.onClick(holder.itemView)
        //create an onclick also if one is already selected to choose multiple from the favourites list
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view), OnClickListener, OnLongClickListener{
        val tripHeadsignTextView : TextView
        val stopNameTextView : TextView
        val arrivalTimeTextView : TextView
        init{
            tripHeadsignTextView = view.findViewById(R.id.favouritesTripheadsignTextView)
            stopNameTextView = view.findViewById(R.id.favouritesStopNameTextView)
            arrivalTimeTextView = view.findViewById(R.id.favouritesBusTimeTextView)
        }

        //create a mode for the entire recycler view, then change behaviour on onclick/onlongclick for each item
        override fun onClick(v: View?) {
            Log.d("CLICKED", "setting on click listener")
            v?.setOnClickListener{
                if (v.tag != null){
                    if (v.tag == "selected"){
                        Log.d("SELECTED TAG", "Tag is selected")
                        v.setOnClickListener{
                            v.resources?.getColor(R.color.dark, null)?.let { v.setBackgroundColor(it) }
                            v.tag = "unselected"
                        }
                    }
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            //todo allow to select the view, so that we can remove from favourites
            //todo goes through a selection checkbox mode
            //if we cancel the selection, then restore normal view

            v?.setOnLongClickListener{
                if (it.tag == null) {
                    v.resources?.getColor(R.color.white, null)?.let { v.setBackgroundColor(it) }
                    v.tag = "selected"
                }
                else if (it.tag != "selected") {
                        v.resources?.getColor(R.color.white, null)?.let { v.setBackgroundColor(it) }
                        v.tag = "selected"
                }
                true
            }
            return false
        }
    }
}