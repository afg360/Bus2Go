package dev.mainhq.bus2go

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.bus2go.adapters.BusListElemsAdapter
import dev.mainhq.bus2go.database.exo_data.AppDatabaseExo
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.utils.TransitInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

//instead of creating a new intent, just redo the list if search done in this activity
class SearchBus : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.search_bus)
        val extras = intent.extras!!
        val query = extras.getString("query") ?: throw IllegalStateException("There must be a query given to start SearchBus")
        //todo optimisation possible by using the results from mainActivity database query
        lifecycleScope.launch { setup(query) }
        findViewById<View>(R.id.back_button)?.setOnClickListener { finish() }
    }
    
    private suspend fun setup(query : String){
        val dbSTM = Room.databaseBuilder(this, AppDatabaseSTM::class.java, "stm_info")
            .createFromAsset("database/stm_info.db").build()
        val dbExo = Room.databaseBuilder(this, AppDatabaseExo::class.java, "exo_info")
            .createFromAsset("database/exo_info.db").build()
        val jobSTM = lifecycleScope.async {
            val routes = dbSTM.routesDao()
            val list = routes.getBusRouteInfo(FuzzyQuery(query))
            list.toMutableList().map {
                TransitInfo(it.routeId, it.routeName, null, TransitAgency.STM)
            }
        }
        val jobExo = lifecycleScope.async {
            val routes = dbExo.routesDao()
            val list = routes.getBusRouteInfo(FuzzyQuery(query, true))
            list.toMutableList().map {
                TransitInfo(it.routeId.split("-", limit = 2)[1], it.routeName, null, TransitAgency.EXO_OTHER)
            }
        }
        val list = jobSTM.await() + jobExo.await()
        withContext(Dispatchers.Main){
            val recyclerView : RecyclerView = findViewById(R.id.search_recycle_view)
            val layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.adapter = BusListElemsAdapter(list)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = layoutManager
        }
        dbSTM.close()
        dbExo.close()
    }
}

