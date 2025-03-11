package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.TimesActivity
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.StmBusData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel

class StopListElemsAdapterStm(stopNames: List<String>, favourites: List<TransitData>,
							  favouritesViewModel: FavouritesViewModel, private val routeId: String,
							  private val directionId: Int, private val direction: String,
							  private val lastStop: String )
	: StopListElemsAdapter(stopNames, favourites, favouritesViewModel) {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
		return ViewHolder(
			LayoutInflater.from(parent.context).inflate(R.layout.elem_stop_list, parent, false),
			routeId, direction
		)
	}

	override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
		val stopName = stopNames[position]
		holder.stopNameTextView.text = stopName

		val stmData = StmBusData(stopName, routeId, directionId, direction, lastStop)
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


	class ViewHolder(view: View, private val routeId: String, private val direction: String) : InnerViewHolder(view) {
		init {
			stopNameTextView.setOnClickListener {
				val stopName = (it as MaterialTextView).text as String
				val intent = Intent(view.context, TimesActivity::class.java)
				intent.putExtra("stopName", stopName)
				intent.putExtra(BusExtrasInfo.AGENCY.name, TransitAgency.STM)
				intent.putExtra(BusExtrasInfo.ROUTE_ID.name, routeId)
				intent.putExtra(BusExtrasInfo.DIRECTION.name, direction)
				it.context.startActivity(intent)
				it.clearFocus()
			}
		}
	}
}