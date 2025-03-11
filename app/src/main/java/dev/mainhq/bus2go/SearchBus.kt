package dev.mainhq.bus2go

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.bus2go.adapters.BusListElemsAdapter
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.utils.TransitInfo
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

//instead of creating a new intent, just redo the list if search done in this activity
class SearchBus : BaseActivity() {

    private lateinit var roomViewModel: RoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_bus)
        val extras = intent.extras!!
        val query = extras.getString("query") ?: throw IllegalStateException("There must be a query given to start SearchBus")
        //todo optimisation possible by using the results from mainActivity database query
        roomViewModel = ViewModelProvider(this)[RoomViewModel::class.java]
        lifecycleScope.launch { setup(query) }
        findViewById<View>(R.id.back_button)?.setOnClickListener { finish() }
    }
    
    private suspend fun setup(query : String){
        val jobSTM = lifecycleScope.async {
            val list = roomViewModel.queryStmRoutes(FuzzyQuery(query))
            list.toMutableList().map {
                TransitInfo(it.routeId, it.routeName, null, TransitAgency.STM)
            }
        }
        val jobExo = lifecycleScope.async {
            val list = roomViewModel.queryExoRoutes(FuzzyQuery(query, true))
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
    }
}

