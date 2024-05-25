package dev.mainhq.bus2go.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Settings
import dev.mainhq.bus2go.preferences.AlarmsData
import dev.mainhq.bus2go.preferences.AlarmsSerializer
import dev.mainhq.bus2go.utils.adapters.AlarmsListElemAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val Context.alarmDataStore : DataStore<AlarmsData> by dataStore(
    fileName = "alarms.json",
    serializer = AlarmsSerializer
)

/* For the moment, the user can only add an alarm to a favourite bus */
class Alarms : Fragment(R.layout.fragment_alarms) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuBar = view.findViewById<MaterialToolbar>(R.id.alarmsMaterialToolBar)
        menuBar.setOnMenuItemClickListener {
            when (it.itemId){
                /*R.id.addAlarmButton -> {
                    Toast.makeText(view.context, "Hello", Toast.LENGTH_SHORT).show()
                    true
                }*/
                R.id.settingsIcon -> {
                    val intent = Intent(this.context, Settings::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        context?.also {
            val recyclerView : RecyclerView = view.findViewById(R.id.alarmsList)
            val linearLayoutManager = LinearLayoutManager(it)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = linearLayoutManager
            lifecycleScope.launch {
                val list = it.alarmDataStore.data.first().list.toList()
                withContext(Dispatchers.Main){
                    recyclerView.adapter = AlarmsListElemAdapter(list)
                }
            }

        }

        context?.apply {
            //This recycler View is NOT in the xml. it is created for the dialog box specifically
            val recyclerView = RecyclerView(this)
            lifecycleScope.launch {
                val list =  favouritesDataStore.data.first().list.toList()
                withContext(Dispatchers.Main){
                    if (list.isNotEmpty()) {
                        val layoutManager = LinearLayoutManager(view.context)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        recyclerView.layoutManager = layoutManager
                        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {_ ->
                            //Create a popup containing the recyclerview of each buses in favourites
                            context?.also {
                                val transaction = parentFragmentManager.beginTransaction()
                                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                transaction.add(AlarmCreationDialog(), null).commit()
                            }
                        }
                    }
                    else{
                        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener{
                            Toast.makeText(view.context, "No favourites made", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

}