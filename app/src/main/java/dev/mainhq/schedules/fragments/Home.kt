package dev.mainhq.schedules.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.android.material.search.SearchView.TransitionState
import dev.mainhq.schedules.R
import dev.mainhq.schedules.SearchBus
import dev.mainhq.schedules.Settings
import dev.mainhq.schedules.utils.adapters.BusListElemsAdapter
import dev.mainhq.schedules.utils.setup
import kotlinx.coroutines.launch

//FIXME CANNOT SUPPORT VERY FAST CHANGES BETWEEN THIS FRAG AND THE OTHERS -> APP CRASHES BECAUSE NO VIEW IS RETURNED
class Home : Fragment(R.layout.home_fragment) {

    //todo could use a favourites list here to use in other methods
    private lateinit var searchView : SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                childFragmentManager.beginTransaction()
                    .replace(R.id.favouritesFragmentContainer, Favourites()).commit()
            }
        })

        searchView = view.findViewById(R.id.main_search_view)
        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val newText = editable.toString()
                if (newText.isEmpty()){
                    val recyclerView = view.findViewById<RecyclerView>(R.id.search_recycle_view)
                    val layoutManager = LinearLayoutManager(this@Home.context)
                    recyclerView.setBackgroundColor(resources.getColor(R.color.dark, null))
                    recyclerView.adapter = BusListElemsAdapter(ArrayList())
                    recyclerView.layoutManager = layoutManager
                }
                else {
                    lifecycleScope.launch {
                        setup(newText, this@Home, R.color.white)
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
                val intent = Intent(this.context, SearchBus::class.java)
                intent.putExtra("query", submittedText)
                startActivity(intent)
                true
            }
            else {
                false
            }
        }
        searchView.addTransitionListener { searchView, previousState, newState ->
            if (previousState == TransitionState.HIDDEN && newState == TransitionState.SHOWING){
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
                val intent = Intent(this.context, Settings::class.java)
                startActivity(intent)
                true
            }
            else super.onOptionsItemSelected(it)
        }

    }

    //fun onBackPressed() {
    //    if (searchView.isShowing)
    //        searchView.hide()
    //    activity?.onBackPressedDispatcher?.onBackPressed()
    //}
}