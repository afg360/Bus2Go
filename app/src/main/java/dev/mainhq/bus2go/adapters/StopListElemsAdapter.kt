package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Times
import dev.mainhq.bus2go.preferences.ExoBusData
import dev.mainhq.bus2go.preferences.StmBusData
import dev.mainhq.bus2go.preferences.TrainData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel


//FIXME could make this class abstract

object StopListElemsAdapterFactory {

    fun createStmStopListElemsAdapter(stopNames: List<String>, favourites: List<TransitData>,
                                      favouritesViewModel: FavouritesViewModel,
                                      routeId: String, direction: String, directionId: Int, lastStop: String) : StopListElemsAdapter{
        return StopListElemsAdapter(stopNames, favourites, favouritesViewModel, TransitAgency.STM,
            hashMapOf(
                Pair(BusExtrasInfo.ROUTE_ID, routeId),
                Pair(BusExtrasInfo.DIRECTION, direction),
                Pair(BusExtrasInfo.DIRECTION_ID, directionId),
                Pair(BusExtrasInfo.LAST_STOP, lastStop)
            )
        )
    }

    fun createExoTrainStopListElemsAdapter(stopNames: List<String>, favourites: List<TransitData>,
                                      favouritesViewModel: FavouritesViewModel,
                                      routeId: String, routeName: String, directionId: Int, direction: String, trainNum: Int) : StopListElemsAdapter{
        return StopListElemsAdapter(stopNames, favourites, favouritesViewModel, TransitAgency.EXO_TRAIN,
            hashMapOf(
                Pair(BusExtrasInfo.ROUTE_ID, routeId),
                Pair(BusExtrasInfo.ROUTE_NAME, routeName),
                Pair(BusExtrasInfo.DIRECTION_ID, directionId),
                Pair(BusExtrasInfo.DIRECTION, direction),
                Pair(BusExtrasInfo.TRAIN_NUM, trainNum)
            )
        )
    }

    fun createExoOtherStopListElemsAdapter(stopNames: List<String>, favourites: List<TransitData>,
                                      favouritesViewModel: FavouritesViewModel,
                                      routeId: String, routeName: String, direction:String, headsign: String) : StopListElemsAdapter{
        return StopListElemsAdapter(stopNames, favourites, favouritesViewModel, TransitAgency.EXO_OTHER,
            hashMapOf(
                Pair(BusExtrasInfo.ROUTE_ID, routeId),
                Pair(BusExtrasInfo.ROUTE_NAME, routeName),
                Pair(BusExtrasInfo.DIRECTION, direction),
                Pair(BusExtrasInfo.HEADSIGN, headsign)
            )
        )
    }

    class StopListElemsAdapter internal constructor(private val stopNames: List<String>,
                                                    private val favourites: List<TransitData>,
                                                    private val favouritesViewModel: FavouritesViewModel,
                                                    private val agency: TransitAgency,
                                                    private val map: HashMap<BusExtrasInfo, Any>)
        : RecyclerView.Adapter<StopListElemsAdapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.elem_stop_list, parent, false),
                map, agency)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val stopName = stopNames[position]
            holder.stopNameTextView.text = stopName
            /** Initialise the right type of favourite button */
            when (agency) {
                TransitAgency.EXO_TRAIN -> {
                    val trainData = TrainData(stopName,
                            map[BusExtrasInfo.ROUTE_ID] as String,
                            map[BusExtrasInfo.TRAIN_NUM] as Int,
                            map[BusExtrasInfo.ROUTE_NAME] as String,
                            map[BusExtrasInfo.DIRECTION_ID] as Int,
                            map[BusExtrasInfo.DIRECTION] as String
                        )
                    holder.stopNameTextView.setTextColor(holder.itemView.resources.getColor(R.color.orange, null))
                    if (favourites.contains(trainData)) {
                        holder.favouriteSelectedView.tag = "on"
                        holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
                    }
                    holder.favouriteSelectedView.setOnClickListener { view ->
                        if (view.tag.equals("off")) {
                            view.setBackgroundResource(R.drawable.favourite_drawable_on)
                            view.tag = "on"
                            favouritesViewModel.addFavourites(trainData)
                        }
                        else {
                            view.setBackgroundResource(R.drawable.favourite_drawable_off)
                            view.tag = "off"
                            //todo add to favourites
                            favouritesViewModel.removeFavourites(trainData)
                        }
                    }
                }

                TransitAgency.STM -> {
                    val stmData = StmBusData(
                        stopName,
                        map[BusExtrasInfo.ROUTE_ID] as String,
                        map[BusExtrasInfo.DIRECTION_ID] as Int,
                        map[BusExtrasInfo.DIRECTION] as String,
                        map[BusExtrasInfo.LAST_STOP] as String
                    )
                    holder.stopNameTextView.setTextColor(holder.itemView.resources .getColor(R.color.basic_blue, null))
                    if (favourites.contains(stmData)){
                        holder.favouriteSelectedView.tag = "on"
                        holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
                    }
                    holder.favouriteSelectedView.setOnClickListener { view ->
                        if (view.tag.equals("off")) {
                            view.setBackgroundResource(R.drawable.favourite_drawable_on)
                            view.tag = "on"
                            favouritesViewModel.addFavourites(stmData)
                        }
                        else {
                            view.setBackgroundResource(R.drawable.favourite_drawable_off)
                            view.tag = "off"
                            //todo add to favourites
                            favouritesViewModel.removeFavourites(stmData)
                        }
                    }
                }
                TransitAgency.EXO_OTHER -> {
                    val exoBusData = ExoBusData(holder.stopNameTextView.text.toString(),
                        map[BusExtrasInfo.ROUTE_ID] as String,
                        map[BusExtrasInfo.DIRECTION] as String,
                        map[BusExtrasInfo.ROUTE_NAME] as String,
                        map[BusExtrasInfo.HEADSIGN] as String
                        //routeId, direction, routeName!!, headsign!!)
                    )
                    holder.stopNameTextView.setTextColor(holder.itemView.resources .getColor(R.color.basic_purple, null))
                    if (favourites.contains(exoBusData)){
                        holder.favouriteSelectedView.tag = "on"
                        holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
                    }
                    holder.favouriteSelectedView.setOnClickListener { view ->
                        if (view.tag.equals("off")) {
                            view.setBackgroundResource(R.drawable.favourite_drawable_on)
                            view.tag = "on"
                            favouritesViewModel.addFavourites(exoBusData)
                        }
                        else {
                            view.setBackgroundResource(R.drawable.favourite_drawable_off)
                            view.tag = "off"
                            //todo add to favourites
                            favouritesViewModel.removeFavourites(exoBusData)
                        }
                    }
                }
            }

        }

        override fun getItemCount(): Int {
            return this.stopNames.size
        }

        //FIXME also create factory methods
        class ViewHolder(view: View, map: HashMap<BusExtrasInfo, Any>, agency: TransitAgency) : RecyclerView.ViewHolder(view) {
            //TODO INSTEAD ADD A COLUMN IN DATABASE TO SET AS FAVOURITE A CERTAIN STOP, AND AFFICHER ONLY THE NEXT STOP
            val stopNameTextView: MaterialTextView
            val favouriteSelectedView : ImageView
            init {
                stopNameTextView = view.findViewById(R.id.stop)
                stopNameTextView.setOnClickListener{
                    val stopName = (it as MaterialTextView).text as String
                    val intent = Intent(view.context, Times::class.java)
                    intent.putExtra("stopName", stopName)
                    intent.putExtra(BusExtrasInfo.AGENCY.name, agency)
                    when (agency) {
                        //FIXME use TransitData objects instead
                        TransitAgency.EXO_TRAIN -> {
                            intent.putExtra(BusExtrasInfo.ROUTE_ID.name, map[BusExtrasInfo.ROUTE_ID] as String)
                            intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, map[BusExtrasInfo.DIRECTION_ID] as Int)
                            intent.putExtra(BusExtrasInfo.DIRECTION.name, map[BusExtrasInfo.DIRECTION] as String)
                            intent.putExtra(BusExtrasInfo.TRAIN_NUM.name, map[BusExtrasInfo.TRAIN_NUM] as Int)
                        }
                        TransitAgency.STM -> {
                            intent.putExtra(BusExtrasInfo.ROUTE_ID.name, map[BusExtrasInfo.ROUTE_ID] as Int)
                            intent.putExtra(BusExtrasInfo.DIRECTION.name, map[BusExtrasInfo.DIRECTION] as String)
                        }
                        TransitAgency.EXO_OTHER -> {
                            //intent.putExtra("TRANSIT_DATA", TrainData(stopName, "", 0, "", "", ""))
                            //routeId is used for being displayed
                            intent.putExtra(BusExtrasInfo.ROUTE_ID.name, map[BusExtrasInfo.ROUTE_ID] as String)
                            intent.putExtra(BusExtrasInfo.HEADSIGN.name, map[BusExtrasInfo.HEADSIGN] as String)
                        }
                    }
                    it.context.startActivity(intent)
                    it.clearFocus()
                }
                favouriteSelectedView = view.findViewById(R.id.favourite_star_selection)
                favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_off)
                favouriteSelectedView.tag = "off"
            }
        }
    }
}
