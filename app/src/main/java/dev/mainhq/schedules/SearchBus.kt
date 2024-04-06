package dev.mainhq.schedules

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import dev.mainhq.schedules.database.AppDatabase
import dev.mainhq.schedules.utils.Parser
import dev.mainhq.schedules.utils.RecyclerViewItemListener
import dev.mainhq.schedules.utils.RecyclerViewItemListener.ClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.lang.ref.WeakReference

//instead of creating a new intent, just redo the list if search done in this activity
class SearchBus : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.search_bus)
        val extras = this.intent.extras!!
        val query = extras.getString("query") ?: throw IllegalStateException("There must be a query given to start SearchBus")
        val ref = WeakReference(this)
        //todo optimisation possible by using the results from mainActivity database query
        lifecycleScope.launch {
            ref.get()?.let { Parser.setup(query, it, null, null) }
        }
        onClickBack()
    }


    private fun onClickBack() {
        findViewById<View>(R.id.back_button)?.setOnClickListener { finish() }
    }
}
