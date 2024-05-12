package dev.mainhq.schedules

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import dev.mainhq.schedules.fragments.Favourites
import dev.mainhq.schedules.utils.*
import dev.mainhq.schedules.utils.adapters.BusListElemsAdapter
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

//TODO
//when updating the app (especially for new stm txt files), will need
//to show to user storing favourites of "deprecated buses" that it has changed
//to another bus (e.g. 435 -> 465)

class MainActivity : AppCompatActivity() {

    //todo could use a favourites list here to use in other methods
    private lateinit var searchView : SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //todo
        if (false) { //TODO check if config file exists
            val intent = Intent(this.applicationContext, Config::class.java)
            startActivity(intent)
        }
        setContentView(R.layout.main_activity)
        lifecycle.addObserver(object : DefaultLifecycleObserver{
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.favouritesFragmentContainer, Favourites()).commit()
            }
        })
        val weakReference : WeakReference<AppCompatActivity> = WeakReference(this)
        searchView = findViewById(R.id.main_search_view)
        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val newText = editable.toString()
                if (newText.isEmpty()){
                    val recyclerView = findViewById<RecyclerView>(R.id.search_recycle_view)
                    val layoutManager = LinearLayoutManager(applicationContext)
                    recyclerView.setBackgroundColor(resources.getColor(R.color.dark, null))
                    recyclerView.adapter = BusListElemsAdapter(ArrayList())
                    recyclerView.layoutManager = layoutManager
                }
                else {
                    lifecycleScope.launch {
                        weakReference.get()?.let{setup(newText, it, R.color.white)}
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Not needed for our purposes
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Not needed for our purposes
            }
        })
        searchView.editText.setOnEditorActionListener { textView : TextView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val submittedText = textView.text.toString()
                val intent = Intent(applicationContext, SearchBus::class.java)
                intent.putExtra("query", submittedText)
                startActivity(intent)
                true
            }
            else {
                false
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                if (searchView.isShowing)
                    searchView.hide()
            }
        }
        val searchBar : SearchBar = findViewById(R.id.mainSearchBar)
        searchBar.inflateMenu(R.menu.app_bar_menu)
        searchBar.setOnMenuItemClickListener {
            val itemID = it.itemId
            if (itemID == R.id.settingsIcon) {
                val intent = Intent(this, Settings::class.java)
                startActivity(intent)
                true
            }
            else super.onOptionsItemSelected(it)
        }

        val bottomAppBar : BottomAppBar = findViewById(R.id.mainBottomAppBar)
        bottomAppBar.inflateMenu(R.menu.bottom_nav_bar)
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (searchView.isShowing)
            searchView.hide()
        super.onBackPressedDispatcher.onBackPressed()
    }
}