package dev.mainhq.schedules.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
import dev.mainhq.schedules.preferences.BusInfo
import dev.mainhq.schedules.preferences.Favourites
import dev.mainhq.schedules.preferences.SettingsSerializer
import dev.mainhq.schedules.utils.adapters.FavouritesListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val Context.dataStore : DataStore<Favourites> by dataStore(
    fileName = "favourites.json",
    serializer = SettingsSerializer
)

class Favourites : Fragment(R.layout.fragment_favourites) {
    //first get user favourites data
    //then make the recycler adapter with that data (either that or display 'no favourites'

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            val list =  context?.dataStore?.data?.first()?.list?.toList()
            if (list == null){
                TODO("TO IMPLEMENT")
            }
            else if (list.isEmpty()){
                val viewGroup = view as ViewGroup
                viewGroup.removeView(view.findViewById(R.id.favourites_text_view))
                viewGroup.removeView(view.findViewById(R.id.favouritesRecyclerView))
            }
            else setBus(list, view)
        }
    }


    private suspend fun setBus(list : List<BusInfo>, view : View) {
        withContext(Dispatchers.Main){
            val layoutManager = LinearLayoutManager(view.context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val recyclerView : RecyclerView = view.findViewById(R.id.favouritesRecyclerView)
            recyclerView.layoutManager = layoutManager
            //need to improve that code to make it more safe
            recyclerView.adapter = FavouritesListElemsAdapter(list)
        }
    }
}