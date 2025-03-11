package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.TimesActivity
import dev.mainhq.bus2go.data.data_source.local.preference.deprecated.ExoTrainData
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.utils.BusExtrasInfo
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.viewmodels.FavouritesViewModel

class StopListElemsAdapterExoTrain(stopNames: List<String>, favourites: List<TransitData>,
								   favouritesViewModel: FavouritesViewModel, private val routeId: String,
								   private val routeName: String, private val directionId: Int,
								   private val direction: String, private val trainNum : Int)
	: StopListElemsAdapter(stopNames, favourites, favouritesViewModel){

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
		return ViewHolder(
			LayoutInflater.from(parent.context).inflate(R.layout.elem_stop_list, parent, false),
			routeId, directionId, direction, trainNum
		)
	}

	override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
		val stopName = stopNames[position]
		holder.stopNameTextView.text = stopName

		val trainData = ExoTrainData(stopName, routeId, trainNum, routeName, directionId, direction)
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

	class ViewHolder(view: View, private val routeId: String,
					 private val directionId: Int, private val direction: String,
					 private val trainNum: Int) : InnerViewHolder(view){
		init {
			stopNameTextView.setOnClickListener {
				val stopName = (it as MaterialTextView).text as String
				val intent = Intent(view.context, TimesActivity::class.java)
				intent.putExtra("stopName", stopName)
				intent.putExtra(BusExtrasInfo.AGENCY.name, TransitAgency.EXO_TRAIN)
				intent.putExtra(BusExtrasInfo.ROUTE_ID.name, routeId)
				intent.putExtra(BusExtrasInfo.DIRECTION_ID.name, directionId)
				intent.putExtra(BusExtrasInfo.DIRECTION.name, direction)
				intent.putExtra(BusExtrasInfo.TRAIN_NUM.name, trainNum)
				it.context.startActivity(intent)
				it.clearFocus()
			}
		}
	}
}