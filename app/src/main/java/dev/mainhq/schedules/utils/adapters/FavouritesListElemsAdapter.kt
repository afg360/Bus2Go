package dev.mainhq.schedules.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
import dev.mainhq.schedules.fragments.FavouriteBusInfo
import dev.mainhq.schedules.utils.Time

class FavouritesListElemsAdapter(private val list : List<FavouriteBusInfo>, private val recyclerView: RecyclerView)
    : RecyclerView.Adapter<FavouritesListElemsAdapter.ViewHolder>() {

        //we may introduce a private field to set the mode of selection
        private var MODE = "unselected"

        //FIXME shitty implementation for now... using a direct reference
        //could use that field above instead...


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //FIXME check if parent can provide me the recyclerview instead
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.favourites_list_elem, parent, false), recyclerView //see if other way...
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
        holder.timeRemainingTextView.text = getTimeRemaining(info.arrivalTime)

        holder.onLongClick(holder.itemView)
        //holder.itemView.seton
        holder.onClick(holder.itemView)
        //create an onclick also if one is already selected to choose multiple from the favourites list
    }

    fun updateTime(viewGroup : ViewGroup, favouritesBusInfo: FavouriteBusInfo){
        //arrivalTimeTextView
        (viewGroup[3] as TextView).text = favouritesBusInfo.arrivalTime.toString()
        //timeRemainingTextView
        (viewGroup[2] as TextView).text = getTimeRemaining(favouritesBusInfo.arrivalTime)
    }

    private fun getTimeRemaining(arrivalTime: Time): String {
        val remainingTime = arrivalTime.timeRemaining() ?: Time(0, 0, 0) //todo replace that for better handling
        return if (remainingTime.hour > 0) "In ${remainingTime.hour} h, ${remainingTime.min} min"
                else "In ${remainingTime.min} min"
    }

    fun unSelect(view : View){
        val viewGroup = view as ViewGroup
        viewGroup.resources?.getColor(R.color.dark, null)?.let { view.setBackgroundColor(it) }
        viewGroup.tag = "unselected"
        //val fragmentfoo = viewGroup.findViewById<FragmentContainerView>(R.id.fragment_selected_checkbox)
        //val fragment = SelectedFavourites()
        //fragmentfoo.supportFragmentManager.beginTransaction()
        //    .replace(R.id.favouritesFragmentContainer, Favourites()).commit()
    }

    class ViewHolder(view : View, private val recyclerView: RecyclerView) : RecyclerView.ViewHolder(view), OnClickListener, OnLongClickListener{
        //var checkBoxView : ConstraintLayout?
        val tripHeadsignTextView : TextView
        val stopNameTextView : TextView
        val arrivalTimeTextView : TextView
        val timeRemainingTextView : TextView
        init{
            tripHeadsignTextView = view.findViewById(R.id.favouritesTripheadsignTextView)
            stopNameTextView = view.findViewById(R.id.favouritesStopNameTextView)
            arrivalTimeTextView = view.findViewById(R.id.favouritesBusTimeTextView)
            timeRemainingTextView = view.findViewById(R.id.favouritesBusTimeRemainingTextView)
            //checkBoxView = null
        }

        //fixme not working properly
        //create a mode for the entire recycler view, then change behaviour on onclick/onlongclick for each item
        override fun onClick(v: View?) {
            v?.setOnClickListener{
                if (v.tag != null){
                    if (v.tag == "selected"){
                        unSelect(v)
                    }
                    else if (v.tag == "unselected"){
                        if (recyclerView.tag != null){
                            if (recyclerView.tag == "selected"){
                                select(v)
                            }
                        }
                    }

                }
                else if (recyclerView.tag != null){
                    if (recyclerView.tag == "selected"){
                        select(v)
                    }
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            //todo allow to select the view, so that we can remove from favourites
            //todo goes through a selection checkbox mode
            //if we cancel the selection, then restore normal view
            v?.setOnLongClickListener{
                if (v.tag == null) select(v)
                else if (v.tag != "selected") select(v)
                (v.parent as RecyclerView).tag = "selected"
                true
            }
            return false
        }

        fun select(view : View){
            view.resources?.getColor(R.color.white, null)?.let { view.setBackgroundColor(it) }
            view.tag = "selected"
        }

        fun unSelect(view : View){
            view.resources?.getColor(R.color.dark, null)?.let { view.setBackgroundColor(it) }
            view.tag = "unselected"
        }

        fun toCheckBox(view : View){

        }

        private fun selectionMode(){
            TODO("Not Implemented Yet")
        }
    }
}