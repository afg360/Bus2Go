package dev.mainhq.bus2go.utils.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.MainActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Times
import dev.mainhq.bus2go.fragments.FavouriteBusInfo
import dev.mainhq.bus2go.fragments.Home
import dev.mainhq.bus2go.utils.Time

class FavouritesListElemsAdapter(private val list : List<FavouriteBusInfo>, private val recyclerView: RecyclerView)
    : RecyclerView.Adapter<FavouritesListElemsAdapter.ViewHolder>(){

        //FIXME shitty implementation for now... using a direct reference

    var numSelected : Int = 0

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
        holder.onClick(holder.itemView)
        holder.checkBoxView.setOnClickListener {
            val parent = recyclerView.parent.parent.parent.parent.parent.parent as ViewGroup
            if ((it as MaterialCheckBox).isChecked) holder.select(holder.itemView, parent)
            else holder.unSelect(holder.itemView, parent)
            parent.findViewById<MaterialTextView>(R.id.selectedNumsOfFavourites)
                .text = (recyclerView.adapter as FavouritesListElemsAdapter).numSelected.run{
                val deleteItemsWidget = parent.findViewById<LinearLayout>(R.id.removeItemsWidget)
                if (this > 0) {
                    if (deleteItemsWidget.visibility == GONE) deleteItemsWidget.visibility = VISIBLE
                    toString()
                }
                else {
                    deleteItemsWidget.visibility = GONE
                    recyclerView.context.getString(R.string.select_favourites_to_remove)
                }
            }
        }
    }

    fun updateTime(viewGroup : ViewGroup, favouritesBusInfo: FavouriteBusInfo){
        val container = viewGroup[0] as ViewGroup
        ((container[1] as ViewGroup)[2] as MaterialTextView).text = favouritesBusInfo.arrivalTime.toString()
        ((container[1] as ViewGroup)[1] as MaterialTextView).text = getTimeRemaining(favouritesBusInfo.arrivalTime)
    }

    private fun getTimeRemaining(arrivalTime: Time): String {
        val remainingTime = arrivalTime.timeRemaining() ?: Time(0, 0, 0) //todo replace that for better handling
        return if (remainingTime.hour > 0) "In ${remainingTime.hour} h, ${remainingTime.min} min"
                else "In ${remainingTime.min} min"
    }

    /** This function is used to deselect a container
     *  containing the favourite element in the view group.
     *  Mainly used with configuring the back button callbacks, to deselect every items  */
    fun unSelect(/** Outer view group layout, containing the linear layout (at the moment) for the other components */
                 viewGroup : ViewGroup){
        viewGroup.tag?.also {
            if (it != "unselected") {
                viewGroup.tag = "unselected"
                /** Deselect the checkbox so that next time doesnt need to press twice to deselect it */
                ((viewGroup[0] as ViewGroup)[0] as MaterialCheckBox).isChecked = false
                numSelected--
            }
        }
    }

    fun select(/** Outer view group layout, containing the linear layout (at the moment) for the other components */
               viewGroup : ViewGroup){
        if (viewGroup.tag != "selected"){
            viewGroup.tag = "selected"
            /** Deselect the checkbox so that next time doesnt need to press twice to deselect it */
            ((viewGroup[0] as ViewGroup)[0] as MaterialCheckBox).isChecked = true
            numSelected++
        }
    }

    fun isSelected(/** Outer view group layout, containing the linear layout (at the moment) for the other components */
               viewGroup : ViewGroup) : Boolean{
        return viewGroup.tag == "selected"
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
        /** This onClick either serves to check the remaining times if not in selection mode, or to select/unselect an item of the
         *  recycler view if in selection mode (see onLongClick for more detail on the selection mode) */
        override fun onClick(v: View?) {
            //TODO give the all checkbox a tag. if tag == allSelected, then deselect it if we unselect at least one
            //FIXME Refactor code to have less lines taken
            val parent = (recyclerView.parent.parent.parent.parent.parent.parent as ViewGroup)
            v?.setOnClickListener{
                //FIXME instead of using tags could simply use materialcheckbox.isChecked
                if (it.tag != null){
                    when(it.tag){
                        "selected" -> {
                            unSelect(it, parent)
                            checkBoxView.isChecked = false
                        }
                        "unselected" -> {
                            recyclerView.tag?.let{tag ->
                                when(tag){
                                    "selected" -> {
                                        select(it, parent)
                                        checkBoxView.isChecked = true
                                    }
                                    "unselected" -> startTimes(v)
                                }
                            }
                        }
                    }
                }
                else if (recyclerView.tag != null){
                    when(recyclerView.tag){
                        "selected" -> {
                            select(it, parent)
                            checkBoxView.isChecked = true
                        }
                        "unselected" -> startTimes(v)
                    }
                }
                else startTimes(v)
                parent.findViewById<MaterialTextView>(R.id.selectedNumsOfFavourites)
                    .text = (recyclerView.adapter as FavouritesListElemsAdapter).numSelected.run{
                    val deleteItemsWidget = parent.findViewById<LinearLayout>(R.id.removeItemsWidget)
                    if (this > 0) {
                        if (deleteItemsWidget.visibility == GONE) deleteItemsWidget.visibility = VISIBLE
                        toString()
                    }
                    else {
                        deleteItemsWidget.visibility = GONE
                        recyclerView.context.getString(R.string.select_favourites_to_remove)
                    }
                }
            }
        }

        /** This onLongClick function serves as a selection interface (entering selection mode) for the recycler view items, so that
         * we can perform operations on them. */
        override fun onLongClick(v: View?): Boolean {
            //TODO is v : View only the item clicked, or the whole thing? may need to retrieve the whole parent and set its tag only
            //FIXME need to update the select all button ONLY IF one item in the list
            /** This next line will attempt to change the appbar to allow deletions */
            val parent = (recyclerView.parent.parent.parent.parent.parent.parent as ViewGroup)
            v?.setOnLongClickListener{
                ((it.context as MainActivity).findViewById<FragmentContainerView>(R.id.mainFragmentContainer)
                    .getFragment() as Home).view?.findViewById<AppBarLayout>(R.id.mainAppBar)
                    ?.apply{
                        /** This is the search bar that will disappear in the appBar*/
                        children.elementAt(0).visibility = GONE
                        /** This is the constraint layout having the selection mode */
                        children.elementAt(1).visibility = VISIBLE
                    }
                val tmpRecyclerView = v.parent as RecyclerView
                val adapter = tmpRecyclerView.adapter as FavouritesListElemsAdapter
                /** if recycler has never been long clicked/has been backed, check if the view is not selected and select it */
                if (tmpRecyclerView.tag == null || tmpRecyclerView.tag == "unselected"){
                    if (v.tag == null || v.tag == "unselected") {
                        select(v, parent)
                        /** Show the checkboxes for each of the favourite elements */
                        recyclerView.forEach {view ->
                            val viewGroup = (view as ViewGroup)[0] as ViewGroup
                            (viewGroup[0] as MaterialCheckBox).visibility = VISIBLE
                            val constraintLayout = viewGroup[1] as ConstraintLayout
                            setMargins(constraintLayout, 10, 15)
                        }
                        checkBoxView.isChecked = true
                    }
                    tmpRecyclerView.tag = "selected"
                    parent.findViewById<MaterialTextView>(R.id.selectedNumsOfFavourites)
                        .text = adapter.numSelected.toString()
                    parent.findViewById<LinearLayout>(R.id.removeItemsWidget).apply{
                        if (visibility == GONE) visibility = VISIBLE
                    }
                }
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

        fun select(view : View, parent : ViewGroup?){
            parent?.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox)?.apply {
                (recyclerView.adapter as FavouritesListElemsAdapter).also {
                    if (it.numSelected == it.itemCount - 1) {
                        if (!isChecked) isChecked = true
                    }
                }
            }
            view.tag = "selected"
            (recyclerView.adapter as FavouritesListElemsAdapter).numSelected++
        }

        fun unSelect(view : View, parent : ViewGroup?){
            parent?.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox)?.apply {
                    if (isChecked) isChecked = false
            }
            view.tag = "unselected"
            (recyclerView.adapter as FavouritesListElemsAdapter).numSelected--
        }

    }
}

/** Set the margins for the left margin for the left most items
 *  and the right margin for the right most items inside the recycler view
 */
fun setMargins(constraintLayout : ConstraintLayout, left : Int, right : Int){
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