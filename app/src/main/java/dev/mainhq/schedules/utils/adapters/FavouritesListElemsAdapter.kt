package dev.mainhq.schedules.utils.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.marginStart
import androidx.core.view.setMargins
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
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
        val container = viewGroup[0] as ViewGroup
        ((container[1] as ViewGroup)[2] as MaterialTextView).text = favouritesBusInfo.arrivalTime.toString()
        //timeRemainingTextView
        ((container[1] as ViewGroup)[1] as MaterialTextView).text = getTimeRemaining(favouritesBusInfo.arrivalTime)
    }

    private fun getTimeRemaining(arrivalTime: Time): String {
        val remainingTime = arrivalTime.timeRemaining() ?: Time(0, 0, 0) //todo replace that for better handling
        return if (remainingTime.hour > 0) "In ${remainingTime.hour} h, ${remainingTime.min} min"
                else "In ${remainingTime.min} min"
    }

    fun unSelect(view : View){
        val viewGroup = view as ViewGroup
        //viewGroup.resources?.getColor(R.color.dark, null)?.let { view.setBackgroundColor(it) }
        viewGroup.tag = "unselected"
        //val fragmentfoo = viewGroup.findViewById<FragmentContainerView>(R.id.fragment_selected_checkbox)
        //val fragment = SelectedFavourites()
        //fragmentfoo.supportFragmentManager.beginTransaction()
        //    .replace(R.id.favouritesFragmentContainer, Favourites()).commit()
    }

    class ViewHolder(view : View, private val recyclerView: RecyclerView) : RecyclerView.ViewHolder(view), OnClickListener, OnLongClickListener{
        var checkBoxView : MaterialCheckBox
        val tripHeadsignTextView : MaterialTextView
        val stopNameTextView : MaterialTextView
        val arrivalTimeTextView : MaterialTextView
        val timeRemainingTextView : MaterialTextView
        init{
            tripHeadsignTextView = view.findViewById(R.id.favouritesTripheadsignTextView)
            stopNameTextView = view.findViewById(R.id.favouritesStopNameTextView)
            arrivalTimeTextView = view.findViewById(R.id.favouritesBusTimeTextView)
            timeRemainingTextView = view.findViewById(R.id.favouritesBusTimeRemainingTextView)
            checkBoxView = view.findViewById(R.id.favourites_check_box)
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
            //TODO is v : View only the item clicked, or the whole thing? may need to retrieve the whole parent and set its tag only
            v?.setOnLongClickListener{
                val tmpRecyclerView = v.parent as RecyclerView
                if (tmpRecyclerView.tag == null || tmpRecyclerView.tag == "unselected"){
                    if (v.tag == null || v.tag == "unselected") {
                        select(v)
                        //FIXME need to spawn instead of hide
                        recyclerView.forEach {
                            val viewGroup = (it as ViewGroup)[0] as ViewGroup
                            (viewGroup[0] as MaterialCheckBox).visibility = VISIBLE
                            val constraintLayout = viewGroup[1] as ConstraintLayout
                            setMargins(constraintLayout, 10, 15)
                        }
                        checkBoxView.isChecked = true
                    }
                    tmpRecyclerView.tag = "selected"
                }
                /** if recycler is already selected then simply select the view */
                else select(v)

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
            //view.resources?.getColor(R.color.white, null)?.let { view.setBackgroundColor(it) }
            view.tag = "selected"
        }

        fun unSelect(view : View){
            //view.resources?.getColor(R.color.dark, null)?.let { view.setBackgroundColor(it) }
            view.tag = "unselected"
        }

    }
}
fun setMargins(constraintLayout : ConstraintLayout, left : Int, right : Int){
    /** This function is used to set the margins for the left margin for the left most items
     *  and the right margin for the right most items inside the recycler view
     */
    for (i in 0..<constraintLayout.size){
        val materialTextView = constraintLayout[i] as MaterialTextView
        if (i == 0 || i == 3){
            (materialTextView.layoutParams as ViewGroup.MarginLayoutParams)
                .setMargins(left, 0, 0, 0)
        }
        else if (i == 1 || i == 2){
            (materialTextView.layoutParams as ViewGroup.MarginLayoutParams)
                .setMargins(0, 0, right, 0)
        }
    }
}