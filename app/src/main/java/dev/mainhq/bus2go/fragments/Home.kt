package dev.mainhq.bus2go.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.android.material.search.SearchView.TransitionState
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.SearchBus
import dev.mainhq.bus2go.SettingsActivity
import dev.mainhq.bus2go.adapters.BusListElemsAdapter
import dev.mainhq.bus2go.utils.FuzzyQuery
import dev.mainhq.bus2go.utils.TransitAgency
import dev.mainhq.bus2go.utils.TransitInfo
import dev.mainhq.bus2go.viewmodels.RoomViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//We must have an empty constructor and instead pass elements inside the bundle??
class Home() : Fragment(R.layout.fragment_home) {
    //todo could use a favourites list here to use in other methods
    private lateinit var searchView : SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roomViewModel = ViewModelProvider(this)[RoomViewModel::class.java]
        //TODO check wtf this code does again... the refreshing seems to get fucked when the bus just passed (which is why it shows 0min even for the new bus)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && isAdded) {
                    childFragmentManager.beginTransaction()
                        .replace(
                            R.id.favouritesFragmentContainer,
                            Favourites()
                        ).commit()
                }
            }
        })

        searchView = view.findViewById(R.id.main_search_view)
        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                editable.toString().also{ query ->
                    if (query.isEmpty()){
                        val recyclerView = view.findViewById<RecyclerView>(R.id.search_recycle_view)
                        val layoutManager = LinearLayoutManager(this@Home.context)
                        recyclerView.adapter = BusListElemsAdapter(ArrayList())
                        recyclerView.layoutManager = layoutManager
                    }
                    else {
                        lifecycleScope.launch {
                            val jobSTM = async {
                                val list = roomViewModel.queryStmRoutes(FuzzyQuery(query))
                                list.toMutableList().map {
                                    TransitInfo(it.routeId, it.routeName, null, TransitAgency.STM)
                                }
                            }
                            val jobExo = async {
                                //TODO FIRST CHECK IF IT IS A TRAIN OR SOMETHING ELSE
                                val list = roomViewModel.queryExoRoutes(FuzzyQuery(query, true))
                                list.toMutableList().map {
                                    val tmp = it.routeId.split("-", limit = 2)
                                    if (tmp[0] == "trains") {
                                        val values = it.routeName.split(" - ", limit = 2)
                                        TransitInfo(
                                            tmp[1],
                                            /** Parsed train name */
                                            values[1],
                                            /** Train number (WHICH IS != TO THE ROUTE_ID */
                                            values[0].toInt(),
                                            TransitAgency.EXO_TRAIN)
                                    }
                                    else TransitInfo(tmp[1], it.routeName, null, TransitAgency.EXO_OTHER)
                                }
                            }
                            val list = jobSTM.await() + jobExo.await()
                            withContext(Dispatchers.Main){
                                val recyclerView : RecyclerView = requireView().findViewById(R.id.search_recycle_view)
                                val layoutManager = LinearLayoutManager(requireContext().applicationContext)
                                recyclerView.adapter = BusListElemsAdapter(list)
                                layoutManager.orientation = LinearLayoutManager.VERTICAL
                                recyclerView.layoutManager = layoutManager
                            }
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Not needed for our purposes
            }
        })
        searchView.editText.setOnEditorActionListener { textView : TextView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val submittedText = textView.text.toString()
                val intent = Intent(this.context, SearchBus::class.java)
                intent.putExtra("query", submittedText)
                startActivity(intent)
                true
            }
            else {
                false
            }
        }
        /** This part hides the bottom navigation bar when expanding the search bar to the search view */
        searchView.addTransitionListener { _, previousState, newState ->
            if (previousState == TransitionState.HIDDEN && newState == TransitionState.SHOWING){
                //can add an animation
                activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.visibility = View.INVISIBLE
            }
            else if (previousState == TransitionState.SHOWN && newState == TransitionState.HIDING){
                activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.visibility = View.VISIBLE
            }
        }

        //if (android.os.Build.VERSION.SDK_INT >= 33) {
        //    activity?.onBackInvokedDispatcher?.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
        //        if (searchView.isShowing)
        //            searchView.hide()
        //    }
        //}

        val searchBar : SearchBar = view.findViewById(R.id.mainSearchBar)

        searchBar.setOnMenuItemClickListener {
            val itemID = it.itemId
            if (itemID == R.id.settingsIcon) {
                val intent = Intent(this.context, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else super.onOptionsItemSelected(it)
        }
    }

    fun onBackPressed() {
        if (searchView.currentTransitionState == TransitionState.SHOWN){
            searchView.hide()
        }
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<AppBarLayout>(R.id.mainAppBar)?.apply {
            children.elementAt(0).visibility = View.VISIBLE
            children.elementAt(1).visibility = View.GONE
        }
    }
}