package dev.mainhq.bus2go.presentation.choose_stop

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.preferences.TransitData
import dev.mainhq.bus2go.presentation.main.home.favourites.FavouritesViewModel


abstract class StopListElemsAdapter (protected val stopNames: List<String>,
                                    protected val favourites: List<TransitData>,
                                    protected val favouritesViewModel: FavouritesViewModel
)
    : RecyclerView.Adapter<StopListElemsAdapter.InnerViewHolder>() {

    override fun getItemCount(): Int {
        return this.stopNames.size
    }

    abstract class InnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //TODO INSTEAD ADD A COLUMN IN DATABASE TO SET AS FAVOURITE A CERTAIN STOP, AND AFFICHER ONLY THE NEXT STOP
        val stopNameTextView: MaterialTextView = view.findViewById(R.id.stop)
        val favouriteSelectedView : ImageView = view.findViewById(R.id.favourite_star_selection)

        init {
            favouriteSelectedView.setBackgroundResource(R.drawable.favourite_drawable_off)
            favouriteSelectedView.tag = "off"
        }

    }

}
