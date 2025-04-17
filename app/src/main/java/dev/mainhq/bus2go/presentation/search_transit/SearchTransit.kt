package dev.mainhq.bus2go.presentation.search_transit

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.presentation.choose_direction.ChooseDirection
import dev.mainhq.bus2go.presentation.main.home.BusListElemsAdapter
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

//instead of creating a new intent, just redo the list if search done in this activity
//TODO may instead extend the search fragment thingy, instead of being an activity
class SearchTransit : BaseActivity() {

    //FIXME this may be overkill for this simple activity...

    private val searchTransitViewModel: SearchTransitViewModel by viewModels{
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(SearchTransitViewModel::class.java)){
                    return SearchTransitViewModel(
                        (this@SearchTransit.application as Bus2GoApplication).commonModule.getRouteInfo,
                    ) as T
                }
                throw IllegalArgumentException("Gave wrong ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_transit)
        val query = intent?.extras?.getString(ExtrasTagNames.QUERY) ?: throw IllegalStateException("There must be a query given to start SearchBus")
        searchTransitViewModel.queryRouteInfo(query)
        findViewById<MaterialTextView>(R.id.query_result_text).text = "Results for $query"

        val recyclerView : RecyclerView = findViewById(R.id.search_recycle_view)
        val layoutManager = LinearLayoutManager(applicationContext)
        val adapter = BusListElemsAdapter(searchTransitViewModel.routeInfo.value){ data ->
            val intent = Intent(this, ChooseDirection::class.java)
            intent.putExtra(ExtrasTagNames.ROUTE_INFO, data)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                searchTransitViewModel.routeInfo.collect{ routeInfo ->
                    adapter.updateData(routeInfo)
                }
            }
        }
    }
    
}

