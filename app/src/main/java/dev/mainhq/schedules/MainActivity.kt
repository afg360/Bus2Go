package dev.mainhq.schedules

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.database.dao.BusRouteInfo
import dev.mainhq.schedules.utils.Parser
import dev.mainhq.schedules.utils.RecyclerViewItemListener
import dev.mainhq.schedules.utils.RecyclerViewItemListener.ClickListener
import dev.mainhq.schedules.utils.adapters.BusListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Arrays
import java.util.Objects

/*TODO bug to fix for recyclerOnClickListener:
//for some reason, 4 activities made on top of each other when clicking
//so going back comes back to same activity
//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)
*/
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //todo
        if (false) { //check if config file exists
            val intent = Intent(this.applicationContext, Config::class.java)
            startActivity(intent)
        }
        setContentView(R.layout.main_activity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = this.menuInflater
        inflater.inflate(R.menu.app_bar_menu, menu)
        val searchItem = menu.findItem(R.id.app_bar_search_icon)
        val searchView = searchItem.actionView as SearchView
        //set attr so that on back removes the recycler view
        //searchView.on
        listenSearchQuery(searchView)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemID = item.itemId
        return if (itemID == R.id.settingsIcon) {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            return true
        }
        else super.onOptionsItemSelected(item)
    }

    //could put this in a generic class
    private fun listenSearchQuery(searchView: SearchView) {
        val curActivity = WeakReference(this)
        searchView.queryHint = "Search for bus lines, bus nums, etc."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                //todo we will do db operations on next activity
                //val dataQueried = Parser.setupBusRoutes(curr, Parser.DataType.busList, query)
                val intent = Intent(applicationContext, SearchBus::class.java)
                intent.putExtra("query", query)
                //intent.putExtra("STMBusData", dataQueried)
                startActivity(intent)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()){
                    val recyclerView = findViewById<RecyclerView>(R.id.search_recycle_view)
                    val layoutManager = LinearLayoutManager(applicationContext)
                    recyclerView.setBackgroundColor(resources.getColor(R.color.dark, null))
                    recyclerView.adapter = BusListElemsAdapter(ArrayList())
                    recyclerView.layoutManager = layoutManager
                }
                else {
                    lifecycleScope.launch {
                        curActivity.get()?.let{Parser.setup(newText, it, searchView, R.color.white)}
                    }
                }
                return true
            }
        })
    }

}