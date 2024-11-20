package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.AGENCY
import dev.mainhq.bus2go.DIRECTION
import dev.mainhq.bus2go.DIRECTION_ID
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.ROUTE_ID
import dev.mainhq.bus2go.Times
import dev.mainhq.bus2go.preferences.ExoBusData
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel


class StopListElemsAdapter(private val stopNames: List<String>, private val list: List<TransitData>,
                           private val headsign: String?, private val routeId: String?,
                           private val trainNum : Int?, private val routeName : String?,
                           private val directionId : Int?, private val direction : String,
                           private val lastStop : String?, private val agency: TransitAgency,
                           private val favouritesViewModel: FavouritesViewModel)
    : RecyclerView.Adapter<StopListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.elem_stop_list, parent, false),
            headsign, routeId, directionId, direction, agency
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stopName = stopNames[position]
        holder.stopNameTextView.text = stopName
        /** Initialise the right type of favourite button */
        when (agency) {
            TransitAgency.EXO_TRAIN -> {
                holder.stopNameTextView.setTextColor(holder.itemView.resources .getColor(R.color.orange, null))
                if (list.contains(TrainData(stopName, routeId!!, trainNum!!, routeName!!, directionId!!, direction))){
                    holder.favouriteSelectedView.tag = "on"
                    holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
                }
                holder.favouriteSelectedView.setOnClickListener { view ->
                    if (view.tag.equals("off")) {
                        view.setBackgroundResource(R.drawable.favourite_drawable_on)
                        view.tag = "on"
                        favouritesViewModel.addFavourites(TrainData(stopName, routeId, trainNum, routeName, directionId, direction))
                    } else {
                        view.setBackgroundResource(R.drawable.favourite_drawable_off)
                        view.tag = "off"
                        //todo add to favourites
                        favouritesViewModel.removeFavourites(TrainData(stopName, routeId, trainNum, routeName, directionId, direction))
                    }
                }
            }
            TransitAgency.STM -> {
                holder.stopNameTextView.setTextColor(holder.itemView.resources .getColor(R.color.basic_blue, null))
                if (list.contains(StmBusData(stopName, routeId!!, directionId!!, direction, lastStop!!))){
                    holder.favouriteSelectedView.tag = "on"
                    holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
                }
                holder.favouriteSelectedView.setOnClickListener { view ->
                    if (view.tag.equals("off")) {
                        view.setBackgroundResource(R.drawable.favourite_drawable_on)
                        view.tag = "on"
                        favouritesViewModel.addFavourites(StmBusData(stopName, routeId, directionId, direction, lastStop))
                    }
                    else {
                        view.setBackgroundResource(R.drawable.favourite_drawable_off)
                        view.tag = "off"
                        //todo add to favourites
                        favouritesViewModel.removeFavourites(StmBusData(stopName, routeId, directionId, direction, lastStop))
                    }
                }
            }
            TransitAgency.EXO_OTHER -> {
                holder.stopNameTextView.setTextColor(holder.itemView.resources .getColor(R.color.basic_purple, null))
                if (list.contains(ExoBusData(holder.stopNameTextView.text.toString(), headsign!!, direction))){
                    holder.favouriteSelectedView.tag = "on"
                    holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
                }
                holder.favouriteSelectedView.setOnClickListener { view ->
                    if (view.tag.equals("off")) {
                        view.setBackgroundResource(R.drawable.favourite_drawable_on)
                        view.tag = "on"
                        favouritesViewModel.addFavourites(ExoBusData(stopName, headsign, direction))
                    }
                    else {
                        view.setBackgroundResource(R.drawable.favourite_drawable_off)
                        view.tag = "off"
                        //todo add to favourites
                        favouritesViewModel.removeFavourites(ExoBusData(stopName, headsign, direction))
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return this.stopNames.size
    }

    class ViewHolder(view: View, private val headsign : String?, private val routeId : String?,
                     private val directionId : Int?, private val direction: String?,
                     private val agency: TransitAgency) : RecyclerView.ViewHolder(view) {
        //TODO INSTEAD ADD A COLUMN IN DATABASE TO SET AS FAVOURITE A CERTAIN STOP, AND AFFICHER ONLY THE NEXT STOP
        val stopNameTextView: MaterialTextView
        val favouriteSelectedView : ImageView
        init {
            stopNameTextView = view.findViewById(R.id.stop)
            //FIXME NEED TO ADD SOME SHIT OVER HERE FOR THE INTENTS
            stopNameTextView.setOnClickListener{
                val stopName = (it as MaterialTextView).text as String
                val intent = Intent(view.context, Times::class.java)
                intent.putExtra("stopName", stopName)
                when (agency) {
                    TransitAgency.EXO_TRAIN -> {
                        intent.putExtra(DIRECTION_ID, directionId!!)
                        intent.putExtra(ROUTE_ID, routeId!!)
                    }
                    TransitAgency.STM -> {
                        intent.putExtra(DIRECTION, direction!!)
                        intent.putExtra(ROUTE_ID, routeId!!)
                    }
                    else -> intent.putExtra("headsign", headsign!!)
                }
                intent.putExtra(AGENCY, agency)
                it.context.startActivity(intent)
                it.clearFocus()
            }
            favouriteSelectedView = view.findViewById(R.id.favourite_star_selection)
            favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_off)
            favouriteSelectedView.tag = "off"
        }
    }
}