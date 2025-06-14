package dev.mainhq.bus2go.presentation.stop_direction.stop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.TransitData


class StopListElemsAdapter (
    private var transitData: List<TransitData>,
    private val favourites: List<TransitData>,
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
                .setTextColor(holder.itemView.resources .getColor(R.color.basic_purple, null))

            is ExoTrainItem -> holder.stopNameTextView
                .setTextColor(holder.itemView.resources.getColor(R.color.orange, null))

            is StmBusItem -> holder.stopNameTextView
                .setTextColor(holder.itemView.resources.getColor(R.color.basic_blue, null))
        }
        holder.favouriteSelectedView.setOnClickListener {
            toggleFavouritesClickListener(it, data)
        }
        holder.stopNameTextView.setOnClickListener { onClickListener(data) }
        if (favourites.contains(data))
            holder.favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_on)
    }

    fun update(transitData: List<TransitData>){
        this.transitData = transitData
        notifyItemRangeChanged(0, itemCount)
    }

    override fun getItemCount(): Int {
        return this.transitData.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //TODO INSTEAD ADD A COLUMN IN DATABASE TO SET AS FAVOURITE A CERTAIN STOP, AND AFFICHER ONLY THE NEXT STOP
        val stopNameTextView: MaterialTextView = view.findViewById(R.id.stop)
        val favouriteSelectedView: ImageView = view.findViewById(R.id.favourite_star_selection)

        init {
            favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_off)
        }

    }
}
