package dev.mainhq.schedules.utils.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import dev.mainhq.schedules.R
import dev.mainhq.schedules.Times
import dev.mainhq.schedules.fragments.FavouriteBusInfo
import dev.mainhq.schedules.utils.Time

class FavouritesListElemsAdapter(private val list : List<FavouriteBusInfo>, private val recyclerView: RecyclerView)
    : RecyclerView.Adapter<FavouritesListElemsAdapter.ViewHolder>(){

        //FIXME shitty implementation for now... using a direct reference


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
        var checkBoxView : MaterialCheckBox
        val tripHeadsignTextView : TextView
        val stopNameTextView : TextView
        val arrivalTimeTextView : TextView
        val timeRemainingTextView : TextView
        init{
            tripHeadsignTextView = view.findViewById(R.id.favouritesTripheadsignTextView)
            stopNameTextView = view.findViewById(R.id.favouritesStopNameTextView)
            arrivalTimeTextView = view.findViewById(R.id.favouritesBusTimeTextView)
            timeRemainingTextView = view.findViewById(R.id.favouritesBusTimeRemainingTextView)
            checkBoxView = view.findViewById(R.id.favourites_check_box)
            checkBoxView.visibility = View.INVISIBLE
        }

        //fixme not working properly
        //create a mode for the entire recycler view, then change behaviour on onclick/onlongclick for each item
        override fun onClick(v: View?) {
            //FIXME Refactor code to have less lines taken
            v?.setOnClickListener{
                if (it.tag != null){
                    when(it.tag){
                        "selected" -> {
                            unSelect(it)
                            checkBoxView.isChecked = false
                        }
                        "unselected" -> {
                            recyclerView.tag?.let{tag ->
                                when(tag){
                                    "selected" -> {
                                        select(it)
                                        checkBoxView.isChecked = true
                                    }
                                    "unselected" -> {
                                        startTimes(v)
                                    }
                                }
                            }
                        }
                    }
                }
                else if (recyclerView.tag != null){
                    when(recyclerView.tag){
                        "selected" -> {
                            select(it)
                            checkBoxView.isChecked = true
                        }
                        "unselected" -> {
                            startTimes(v)
                        }
                    }
                }
                else{
                    startTimes(v)
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            //todo allow to select the view, so that we can remove from favourites
            //todo goes through a selection checkbox mode
            //if we cancel the selection, then restore normal view
            v?.setOnLongClickListener{
                if (v.tag == null) {
                    select(v)
                    //FIXME need to spawn instead of hide
                    recyclerView.forEach {
                        val viewGroup = it as ViewGroup
                        (viewGroup[0] as MaterialCheckBox).visibility = VISIBLE
                    }
                    checkBoxView.isChecked = true
                }
                else if (v.tag != "selected") select(v)
                (v.parent as RecyclerView).tag = "selected"
                true
            }
            return false
        }

        private fun startTimes(view : View){
            val intent = Intent(view.context, Times::class.java)
            intent.putExtra("stopName", stopNameTextView.text as String)
            intent.putExtra("headsign", tripHeadsignTextView.text as String)
            view.context.startActivity(intent)
            view.clearFocus()
        }

        fun select(view : View){
            view.resources?.getColor(R.color.white, null)?.let { view.setBackgroundColor(it) }
            view.tag = "selected"
        }

        fun unSelect(view : View){
            view.resources?.getColor(R.color.dark, null)?.let { view.setBackgroundColor(it) }
            view.tag = "unselected"
        }
    }
}