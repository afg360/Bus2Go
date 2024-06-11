package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.AGENCY
import dev.mainhq.bus2go.DIRECTION_ID
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.ROUTE_ID
import dev.mainhq.bus2go.Times
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.preferences.TrainInfo
import dev.mainhq.bus2go.preferences.TransitInfo
import dev.mainhq.bus2go.utils.BusAgency
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel


class StopListElemsAdapter(private val data: List<String>, private val list: List<TransitInfo>,
                           private val headsign: String?, private val routeId : String?,
                           private val directionId : Int?, private val agency: BusAgency,
                           private val favouritesViewModel: FavouritesViewModel)
    : RecyclerView.Adapter<StopListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.elem_stop_list, parent, false),
            headsign, routeId, directionId, agency
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = this.data[position]
        holder.stopNameTextView.text = data
        /** Initialise the right type of favourite button */
        if (agency == BusAgency.EXO_TRAIN){
            if (list.contains(TrainInfo(holder.stopNameTextView.text.toString(), routeId!!, directionId!!))){
                holder.favouriteSelectedView.tag = "on"
                holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
            }
            holder.favouriteSelectedView.setOnClickListener { view ->
                if (view.tag.equals("off")) {
                    view.setBackgroundResource(R.drawable.favourite_drawable_on)
                    view.tag = "on"
                    favouritesViewModel.addFavouriteTrains(agency, data, routeId, directionId)
                } else {
                    view.setBackgroundResource(R.drawable.favourite_drawable_off)
                    view.tag = "off"
                    //todo add to favourites
                    favouritesViewModel.removeFavouriteTrains(agency, data, routeId, directionId)
                }
            }
        }
        else{
            if (list.contains(BusInfo(holder.stopNameTextView.text.toString(), headsign!!))){
                holder.favouriteSelectedView.tag = "on"
                holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
            }
            holder.favouriteSelectedView.setOnClickListener { view ->
                if (view.tag.equals("off")) {
                    view.setBackgroundResource(R.drawable.favourite_drawable_on)
                    view.tag = "on"
                    favouritesViewModel.addFavourites(agency, data, headsign)
                }
                else {
                    view.setBackgroundResource(R.drawable.favourite_drawable_off)
                    view.tag = "off"
                    //todo add to favourites
                    favouritesViewModel.removeFavouriteBuses(agency, data, headsign)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return this.data.size
    }

    class ViewHolder(view: View, private val headsign : String?, private val routeId : String?,
                     private val directionId : Int?, private val agency: BusAgency) : RecyclerView.ViewHolder(view) {
        //TODO INSTEAD ADD A COLUMN IN DATABASE TO SET AS FAVOURITE A CERTAIN STOP, AND AFFICHER ONLY THE NEXT STOP
        val stopNameTextView: MaterialTextView
        val favouriteSelectedView : ImageView
        init {
            stopNameTextView = view.findViewById(R.id.stop)
            stopNameTextView.setOnClickListener{
                val stopName = (it as MaterialTextView).text as String
                val intent = Intent(view.context, Times::class.java)
                intent.putExtra("stopName", stopName)
                if (agency == BusAgency.EXO_TRAIN) {
                    intent.putExtra(DIRECTION_ID, directionId!!)
                    intent.putExtra(ROUTE_ID, routeId!!)
                }
                else intent.putExtra("headsign", headsign!!)
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