package dev.mainhq.bus2go.presentation.main.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.search.SearchView.TransitionState
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.databinding.FragmentHomeBinding
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.presentation.core.UiState
import dev.mainhq.bus2go.presentation.search_transit.SearchTransit
import dev.mainhq.bus2go.presentation.settings.SettingsActivity
import dev.mainhq.bus2go.presentation.main.home.favourites.FavouritesFragment
import dev.mainhq.bus2go.presentation.main.home.favourites.FavouritesFragmentSharedViewModel
import dev.mainhq.bus2go.presentation.stop_direction.StopDirectionActivity
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import dev.mainhq.bus2go.utils.makeVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.view.isGone
import com.google.android.material.checkbox.MaterialCheckBox
import dev.mainhq.bus2go.utils.launchViewModelCollect
import dev.mainhq.bus2go.utils.makeGone

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

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectAllCheckbox.setOnClickListener{ favouritesSharedViewModel.toggleSelectAllFavourites() }

        childFragmentManager.beginTransaction()
            .replace(R.id.favouritesFragmentContainer, FavouritesFragment())
            .commit()

        //recyclerView for when searching
        val layoutManager = LinearLayoutManager(this@HomeFragment.context)
        val busListAdapter = BusListElemsAdapter(ArrayList()){ data ->
            val intent = Intent(requireContext(), StopDirectionActivity::class.java)
            intent.putExtra(ExtrasTagNames.ROUTE_INFO, data)
            requireContext().startActivity(intent)
        }
        binding.searchRecycleView.adapter = busListAdapter
        binding.searchRecycleView.layoutManager = layoutManager

        binding.mainSearchView.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(query: Editable?) {
                homeFragmentViewModel.onSearchQueryChange(query?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        //use this instead of directly collecting to prevent collection when in background
        launchViewModelCollect(homeFragmentViewModel.searchQuery){ results ->
            when(results){
                is UiState.Error ->
                    Log.d("DATABASE", "You have jack shit: ${results.message}")
                UiState.Loading -> throw IllegalStateException("Wtf")
                is UiState.Success<List<RouteInfo>> ->
                    busListAdapter.updateData(results.data)
                UiState.Init -> TODO()
            }
        }

        //during selection mode, listen to click on select all
        launchViewModelCollect(favouritesSharedViewModel.selectAllFavourites){ isChecked ->
            //binding.selectAllCheckbox.isChecked = isChecked ?: false
            view.findViewById<MaterialCheckBox>(R.id.selectAllCheckbox).isChecked = isChecked ?: false
        }

        launchViewModelCollect(favouritesSharedViewModel.numberFavouritesSelected){ numSelected ->
            //change the appBar number displayed
            binding.selectedNumsOfFavourites.text = if (numSelected > 0) {
                if (binding.removeItemsWidget.isGone) binding.removeItemsWidget.makeVisible()
                numSelected.toString()
            }
            else {
                binding.removeItemsWidget.makeGone()
                requireContext().getString(R.string.select_favourites_to_remove)
            }
        }

        //handling remove selection mode, coming from favourites fragment
        launchViewModelCollect(favouritesSharedViewModel.selectionMode){ removeFavouritesMode ->
            if (removeFavouritesMode){
                /* This is the search bar that will disappear in the appBar */
                binding.mainSearchBar.makeGone()
                /* This is the constraint layout having the selection mode */
                binding.selectionModeBar.makeVisible()
            }
            else {
                binding.mainSearchBar.makeVisible()
                binding.selectionModeBar.makeGone()
                binding.selectAllCheckbox.isChecked = false
            }
        }

        binding.mainSearchView.editText.setOnEditorActionListener { textView : TextView, actionId, _ ->
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

        val onBackPressedCallback = object : OnBackPressedCallback(false){
            override fun handleOnBackPressed() {
                lifecycleScope.launch(Dispatchers.Main) {
                    homeFragmentViewModel.triggerBackPressed()
                }
            }
        }

        /** This part hides the bottom navigation bar when expanding the search bar to the search view */
        binding.mainSearchView.addTransitionListener { _, previousState, newState ->
            if (previousState == TransitionState.HIDDEN && newState == TransitionState.SHOWING){
                //can add an animation
                activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.makeGone()
                onBackPressedCallback.isEnabled = true
            }
            else if (previousState == TransitionState.SHOWN && newState == TransitionState.HIDING){
                activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.visibility = VISIBLE
            }
        }

        binding.mainSearchBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.settingsIcon -> {
                    val intent = Intent(this.context, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    super.onOptionsItemSelected(menuItem)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
          )

        launchViewModelCollect(homeFragmentViewModel.isBackPressed){
            if (binding.mainSearchView.currentTransitionState == TransitionState.SHOWN) {
                binding.mainSearchView.hide()
                onBackPressedCallback.isEnabled = false
            }
        }
    }

    //FIXME keep the last state
    // for now, avoids to make the bottomNav disappear...
    override fun onPause() {
        super.onPause()
        binding.mainSearchBar.makeVisible()
        binding.selectionModeBar.makeGone()
        activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.makeVisible()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}