package dev.mainhq.bus2go

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import dev.mainhq.bus2go.utils.setup
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.lang.ref.WeakReference

//instead of creating a new intent, just redo the list if search done in this activity
class SearchBus : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.search_bus)
        val extras = intent.extras!!
        val query = extras.getString("query") ?: throw IllegalStateException("There must be a query given to start SearchBus")
        val ref = WeakReference(this)
        //todo optimisation possible by using the results from mainActivity database query
        lifecycleScope.launch {
            ref.get()?.let { setup(query, it, null) }
        }
        findViewById<View>(R.id.back_button)?.setOnClickListener { finish() }
    }
}
