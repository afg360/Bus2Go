package dev.mainhq.bus2go.presentation.main.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.android.material.search.SearchView.TransitionState
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.presentation.choose_direction.ChooseDirection
import dev.mainhq.bus2go.presentation.search_transit.SearchTransit
import dev.mainhq.bus2go.presentation.settings.SettingsActivity
import dev.mainhq.bus2go.presentation.main.home.favourites.FavouritesFragment
import dev.mainhq.bus2go.presentation.main.home.favourites.FavouritesFragmentSharedViewModel
import dev.mainhq.bus2go.presentation.utils.ActivityType
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//We must have an empty constructor and instead pass elements inside the bundle??
class HomeFragment: Fragment(R.layout.fragment_home) {

    private val homeFragmentViewModel: HomeFragmentViewModel by viewModels{
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeFragmentViewModel(
                    (this@HomeFragment.requireActivity().application as Bus2GoApplication).commonModule.getRouteInfo,
                ) as T
            }
        }
    }
    private val favouritesSharedViewModel: FavouritesFragmentSharedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox).setOnClickListener{
            favouritesSharedViewModel.toggleSelectAllFavourites()
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.favouritesFragmentContainer, FavouritesFragment())
            .commit()

        //recyclerView for when searching
        val recyclerView = view.findViewById<RecyclerView>(R.id.search_recycle_view)
        val layoutManager = LinearLayoutManager(this@HomeFragment.context)
        val busListAdapter = BusListElemsAdapter(ArrayList()){ data ->
            val intent = Intent(requireContext(), ChooseDirection::class.java)
            intent.putExtra(ExtrasTagNames.ROUTE_INFO, data)
            requireContext().startActivity(intent)
        }
        recyclerView.adapter = busListAdapter
        recyclerView.layoutManager = layoutManager

        val searchView = view.findViewById<SearchView>(R.id.main_search_view)
        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(query: Editable?) {
                homeFragmentViewModel.onSearchQueryChange(query?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        //use this instead of directly collecting to prevent collection when in background
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeFragmentViewModel.searchQuery.collect { results ->
                    busListAdapter.updateData(results)
                }
            }
        }

        //during selection mode, listen to click on select all
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                favouritesSharedViewModel.selectAllFavourites.collect { isChecked ->
                    view.findViewById<AppBarLayout>(R.id.mainAppBar)?.also { appBarLayout ->
                        appBarLayout.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox).isChecked = isChecked ?: false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            //change the appBar number displayed
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                favouritesSharedViewModel.numberFavouritesSelected.collect { numSelected ->
                    val deleteItemsWidget = view.findViewById<LinearLayout>(R.id.removeItemsWidget)
                    view.findViewById<MaterialTextView>(R.id.selectedNumsOfFavourites)
                        .text = if (numSelected > 0) {
                        if (deleteItemsWidget?.visibility == GONE) deleteItemsWidget.visibility = VISIBLE
                        numSelected.toString()
                    }
                    else {
                        deleteItemsWidget?.visibility = GONE
                        recyclerView.context.getString(R.string.select_favourites_to_remove)
                    }
                }
            }
        }

        //handling remove selection mode, coming from favourites fragment
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                favouritesSharedViewModel.selectionMode.collect{ removeFavouritesMode ->
                    //FIXME instead find directly the related view...
                    view.findViewById<AppBarLayout>(R.id.mainAppBar)?.also { appBarLayout ->
                        if (removeFavouritesMode){
                            /** This is the search bar that will disappear in the appBar*/
                            appBarLayout.children.elementAt(0).visibility = GONE
                            /** This is the constraint layout having the selection mode */
                            appBarLayout.children.elementAt(1).visibility = VISIBLE
                        }
                        else {
                            appBarLayout.children.elementAt(0).visibility = VISIBLE
                            appBarLayout.children.elementAt(1).visibility = GONE
                            appBarLayout.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox).isChecked = false
                        }
                    }
                }
            }
        }

        searchView.editText.setOnEditorActionListener { textView : TextView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val submittedText = textView.text.toString()
                val intent = Intent(this.context, SearchTransit::class.java)
                intent.putExtra(ExtrasTagNames.QUERY, submittedText)
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
                activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.visibility = INVISIBLE
            }
            else if (previousState == TransitionState.SHOWN && newState == TransitionState.HIDING){
                activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.visibility = VISIBLE
            }
        }

        view.findViewById<SearchBar>(R.id.mainSearchBar).setOnMenuItemClickListener { menuItem ->
            val itemID = menuItem.itemId
            if (itemID == R.id.settingsIcon) {
                val intent = Intent(this.context, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else super.onOptionsItemSelected(menuItem)
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                lifecycleScope.launch(Dispatchers.Main) {
                    homeFragmentViewModel.triggerBackPressed()
                }
            }
        })


        lifecycleScope.launch(Dispatchers.Main) {
            homeFragmentViewModel.isBackPressed.collect {
                if (searchView.currentTransitionState == TransitionState.SHOWN) {
                    searchView.hide()
                }
            }
        }
    }

    //FIXME keep the last state
    // for now, avoids to make the bottomNav disappear...
    override fun onPause() {
        super.onPause()
        view?.findViewById<SearchBar>(R.id.mainSearchBar)?.visibility = VISIBLE
        view?.findViewById<ConstraintLayout>(R.id.selectionModeBar)?.visibility = GONE
        activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.visibility = VISIBLE
    }

}