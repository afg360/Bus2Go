package dev.mainhq.schedules.utils

import android.content.Context
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.schedules.MainActivity
import dev.mainhq.schedules.R
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.database.dao.BusRouteInfo
import dev.mainhq.schedules.utils.adapters.BusListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

//right now only for the main activities
//todo may use db operations instead

/** Sets up the activity data from the database to be displayed for the user **/
suspend fun setup(query : String, fragment : Fragment, color : Int?){
    val db = Room.databaseBuilder(fragment.requireContext(), AppDatabase::class.java, "stm_info")
        .createFromAsset("database/stm_info.db").build()
    val routes = db.routesDao()
    val list = routes.getBusRouteInfo(FuzzyQuery(query))
    displayBuses(list, fragment, color)
    db.close()
}

private suspend fun displayBuses(list : List<BusRouteInfo>, fragment: Fragment, color : Int?){
    //todo
    //need to handle queries where french accents are needed
    //val parsable = Parser.toParsable(query)
    //todo need to implement fuzzyquery
    withContext(Dispatchers.Main){
        val recyclerView : RecyclerView = fragment.requireView().findViewById(R.id.search_recycle_view)
        val layoutManager = LinearLayoutManager(fragment.requireContext().applicationContext)
        //color?.let { recyclerView.setBackgroundColor(activity.resources.getColor(it, null)) }
        recyclerView.adapter = BusListElemsAdapter(list)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }
}

suspend fun setup(query : String, activity : AppCompatActivity, color : Int?){
    val db = Room.databaseBuilder(activity, AppDatabase::class.java, "stm_info")
        .createFromAsset("database/stm_info.db").build()
    val routes = db.routesDao()
    val list = routes.getBusRouteInfo(FuzzyQuery(query))
    displayBuses(list, activity, color)
    db.close()
}

private suspend fun displayBuses(list : List<BusRouteInfo>, activity: AppCompatActivity, color : Int?){
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

