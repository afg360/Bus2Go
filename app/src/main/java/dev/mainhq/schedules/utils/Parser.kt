package dev.mainhq.schedules.utils

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.schedules.ChooseBus
import dev.mainhq.schedules.R
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.database.dao.BusRouteInfo
import dev.mainhq.schedules.utils.adapters.BusListElemsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

//right now only for the main activities
//todo may use db operations instead
object Parser {

    suspend fun setup(query : String, activity: AppCompatActivity, searchView: SearchView?, color : Int?){
        val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "stm_info")
            .createFromAsset("database/stm_info.db").build()
        val routes = db.routesDao()
        val list = routes.getBusRouteInfo(FuzzyQuery(query))
        displayBuses(list, activity, searchView, color)
        db.close()
    }

    private suspend fun displayBuses(list : List<BusRouteInfo>, activity : AppCompatActivity,
                                     searchView : SearchView?, color : Int?){
        //todo
        //need to handle queries where french accents are needed
        //val parsable = Parser.toParsable(query)
        //todo need to implement fuzzyquery
        withContext(Dispatchers.Main){
            val recyclerView : RecyclerView = activity.findViewById(R.id.search_recycle_view)
            val layoutManager = LinearLayoutManager(activity.applicationContext)
            color?.let { recyclerView.setBackgroundColor(activity.resources.getColor(it, null)) }
            recyclerView.adapter = BusListElemsAdapter(list)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = layoutManager
        }
    }

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
}