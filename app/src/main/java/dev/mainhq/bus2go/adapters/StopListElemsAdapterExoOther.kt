package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.TimesActivity
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoBusData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel

class StopListElemsAdapterExoOther(stopNames: List<String>, favourites: List<TransitData>,
								   favouritesViewModel: FavouritesViewModel,
								   private val routeId: String, private val direction: String,
								   private val routeName: String, private val headsign: String
) : StopListElemsAdapter(stopNames, favourites, favouritesViewModel) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
		return ViewHolder(
			LayoutInflater.from(parent.context).inflate(R.layout.elem_stop_list, parent, false),
			routeId, headsign
		)
	}

	override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
		val stopName = stopNames[position]
		holder.stopNameTextView.text = stopName

		val exoBusData = ExoBusData(holder.stopNameTextView.text.toString(), routeId, direction,
						routeName, headsign)
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

	class ViewHolder(view: View, private val routeId: String, private val headsign: String) : InnerViewHolder(view){
		init {
			stopNameTextView.setOnClickListener {
				val stopName = (it as MaterialTextView).text as String
				val intent = Intent(view.context, TimesActivity::class.java)
				intent.putExtra("stopName", stopName)
				intent.putExtra(BusExtrasInfo.AGENCY.name, TransitAgency.EXO_OTHER)
				//intent.putExtra("TRANSIT_DATA", TrainData(stopName, "", 0, "", "", ""))
				//routeId is used for being displayed
				intent.putExtra(BusExtrasInfo.ROUTE_ID.name, routeId)
				intent.putExtra(BusExtrasInfo.HEADSIGN.name, headsign)
				it.context.startActivity(intent)
				it.clearFocus()
			}
		}
	}
}