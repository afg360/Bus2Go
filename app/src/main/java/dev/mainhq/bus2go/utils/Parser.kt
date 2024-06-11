package dev.mainhq.bus2go.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.adapters.BusListElemsAdapter
import dev.mainhq.bus2go.database.stm_data.AppDatabaseSTM
import dev.mainhq.bus2go.database.exo_data.AppDatabaseExo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.Locale

//right now only for the main activities
//todo may use db operations instead

/** Sets up the activity data from the database and then displays it to the user **/
suspend fun setup(coroutineScope: CoroutineScope, query : String, fragment : Fragment){
    val dbSTM = Room.databaseBuilder(fragment.requireContext(), AppDatabaseSTM::class.java, "stm_info")
        .createFromAsset("database/stm_info.db").build()
    val dbExo =Room.databaseBuilder(fragment.requireContext(), AppDatabaseExo::class.java, "exo_info")
        .createFromAsset("database/exo_info.db").build()
    val jobSTM = coroutineScope.async {
        val routes = dbSTM.routesDao()
        val list = routes.getBusRouteInfo(FuzzyQuery(query))
        list.toMutableList().map {
            BusInfo(it.routeId, it.routeName, TransitAgency.STM)
        }
    }
    val jobExo = coroutineScope.async {
        //TODO FIRST CHECK IF IT IS A TRAIN OR SOMETHING ELSE
        val routes = dbExo.routesDao()
        val list = routes.getBusRouteInfo(FuzzyQuery(query, true))
        list.toMutableList().map {
            val tmp = it.routeId.split("-", limit = 2)
            if (tmp[0] == "trains") BusInfo(tmp[1], it.routeName, TransitAgency.EXO_TRAIN)
            else BusInfo(tmp[1], it.routeName, TransitAgency.EXO_OTHER)
        }
    }
    val list = jobSTM.await() + jobExo.await()
    withContext(Dispatchers.Main){
        val recyclerView : RecyclerView = fragment.requireView().findViewById(R.id.search_recycle_view)
        val layoutManager = LinearLayoutManager(fragment.requireContext().applicationContext)
        recyclerView.adapter = BusListElemsAdapter(list)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }
    dbSTM.close()
    dbExo.close()
}

suspend fun setup(query : String, activity : AppCompatActivity, color : Int?){
    val dbSTM = Room.databaseBuilder(activity, AppDatabaseSTM::class.java, "stm_info")
        .createFromAsset("database/stm_info.db").build()
    val dbExo =Room.databaseBuilder(activity, AppDatabaseExo::class.java, "exo_info")
        .createFromAsset("database/exo_info.db").build()
    val jobSTM = activity.lifecycleScope.async {
        val routes = dbSTM.routesDao()
        val list = routes.getBusRouteInfo(FuzzyQuery(query))
        list.toMutableList().map {
            BusInfo(it.routeId, it.routeName, TransitAgency.STM)
        }
    }
    val jobExo = activity.lifecycleScope.async {
        val routes = dbExo.routesDao()
        val list = routes.getBusRouteInfo(FuzzyQuery(query, true))
        list.toMutableList().map {
            BusInfo(it.routeId.split("-", limit = 2)[1], it.routeName, TransitAgency.EXO_OTHER)
        }
    }
    val list = jobSTM.await() + jobExo.await()
    displayBuses(list, activity, color)
    dbSTM.close()
    dbExo.close()
}

private suspend fun displayBuses(list : List<BusInfo>, activity: AppCompatActivity, color : Int?){
    //todo
    //need to handle queries where french accents are needed
    //val parsable = Parser.toParsable(query)
    //todo need to implement fuzzyquery
    withContext(Dispatchers.Main){
        val recyclerView : RecyclerView = activity.findViewById(R.id.search_recycle_view)
        val layoutManager = LinearLayoutManager(activity.applicationContext)
        //color?.let { recyclerView.setBackgroundColor(activity.resources.getColor(it, null)) }
        recyclerView.adapter = BusListElemsAdapter(list)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }
}

/** Deals with french characters **/
fun toParsable(txt: String): String {
    var str = txt
    str = str.lowercase(Locale.getDefault())
        .replace("'", "")
        .replace("-", "")
        .replace(" ", "")
        .replace("/", "")
        .replace("é", "e")
        .replace("è", "e")
        .replace("ê", "e")
        .replace("ç", "c")
        .replace("î", "i")
        .replace("ô", "o")
        .replace("û", "u")
    return str
}

data class BusInfo(val routeId : String, val routeName : String, val transitAgency: TransitAgency)

enum class TransitAgency : java.io.Serializable{
    STM, EXO_TRAIN, EXO_OTHER
}

