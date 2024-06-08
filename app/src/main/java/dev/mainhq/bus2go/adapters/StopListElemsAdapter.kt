package dev.mainhq.bus2go.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.AGENCY
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Times
import dev.mainhq.bus2go.preferences.BusInfo
import dev.mainhq.bus2go.utils.BusAgency
import dev.mainhq.bus2go.viewmodel.FavouritesViewModel
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.launch


class StopListElemsAdapter(private val data: List<String>, private val list: List<BusInfo>,
                           private val headsign: String, private val agency: BusAgency,
                           private val favouritesViewModel: FavouritesViewModel)
    : RecyclerView.Adapter<StopListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.elem_stop_list, parent, false),
            headsign,
            agency
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = this.data[position]
        holder.stopNameTextView.text = data
        /** Initialise the right type of favourite button */
        if (list.contains(BusInfo(holder.stopNameTextView.text.toString(), headsign))){
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
                favouritesViewModel.removeFavourites(agency, data, headsign)
            }
        }
    }

    override fun getItemCount(): Int {
        return this.data.size
    }

    class ViewHolder(view: View, private val headsign : String, private val agency: BusAgency) : RecyclerView.ViewHolder(view) {
        //TODO INSTEAD ADD A COLUMN IN DATABASE TO SET AS FAVOURITE A CERTAIN STOP, AND AFFICHER ONLY THE NEXT STOP
        val stopNameTextView: MaterialTextView
        val favouriteSelectedView : ImageView
        init {
            stopNameTextView = view.findViewById(R.id.stop)
            stopNameTextView.setOnClickListener{
                val stopName = (it as MaterialTextView).text as String
                val intent = Intent(view.context, Times::class.java)
                intent.putExtra("stopName", stopName)
                intent.putExtra("headsign", headsign)
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