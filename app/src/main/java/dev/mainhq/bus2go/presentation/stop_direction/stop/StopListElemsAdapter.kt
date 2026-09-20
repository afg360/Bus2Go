package dev.mainhq.bus2go.presentation.stop_direction.stop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.R as MaterialRes
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.entity.compareTransitData
import dev.mainhq.bus2go.utils.getMaterialColor


class StopListElemsAdapter (
    private var transitData: List<TransitData>,
    private var favourites: List<FavouriteTransitData>,
    private val toggleFavouritesClickListener: (View, TransitData) -> Unit,
    private val onClickListener: (TransitData) -> Unit
) : RecyclerView.Adapter<StopListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.elem_stop_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = transitData[position]
        holder.stopNameTextView.text = data.stopName

        when(data){
            is ExoBusItem -> holder.stopNameTextView
                .setTextColor(holder.itemView.getMaterialColor(R.attr.colorCustomColor1))

            is ExoTrainItem -> holder.stopNameTextView
                .setTextColor(holder.itemView.getMaterialColor(R.attr.colorCustomColor2))

            is StmBusItem -> holder.stopNameTextView
                .setTextColor(holder.itemView.getMaterialColor(MaterialRes.attr.colorPrimary))
        }
        holder.favouriteSelectedView.setOnClickListener {
            toggleFavouritesClickListener(it, data)
        }
        setFavouriteButton(holder, position)
        holder.itemView.setOnClickListener { onClickListener(data) }
    }

    companion object {
        private const val FAVOURITES_PAYLOAD = "FAVOURITES"
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any?>) {
        if (payloads.isEmpty()){
            onBindViewHolder(holder, position)
        }
        else if (payloads[0] == FAVOURITES_PAYLOAD){
            setFavouriteButton(holder, position)
        }
    }

    fun setFavouriteButton(holder: ViewHolder, position: Int) {
        if (favourites.compareTransitData(transitData[position])) {
            holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
        }
        else {
            holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_off)
        }
    }

    fun updateTransitData(transitData: List<TransitData>){
        this.transitData = transitData
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateFavourites(favourites: List<FavouriteTransitData>){
        this.favourites = favourites
        notifyItemRangeChanged(0, itemCount, FAVOURITES_PAYLOAD)
    }

    override fun getItemCount(): Int {
        return this.transitData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //TODO INSTEAD ADD A COLUMN IN DATABASE TO SET AS FAVOURITE A CERTAIN STOP, AND AFFICHER ONLY THE NEXT STOP
        val stopNameTextView: MaterialTextView = view.findViewById(R.id.stop)
        val favouriteSelectedView: AppCompatImageButton = view.findViewById(R.id.favourite_star_selection)

        init {
            favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_off)
        }

    }
}
