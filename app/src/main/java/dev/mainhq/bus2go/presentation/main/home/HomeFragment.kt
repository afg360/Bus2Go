package dev.mainhq.bus2go.presentation.main.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.search.SearchView.TransitionState
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.databinding.FragmentHomeBinding
import dev.mainhq.bus2go.domain.entity.FavouriteTransitData
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.exceptions.Bus2GoBaseException
import dev.mainhq.bus2go.presentation.core.UiState
import dev.mainhq.bus2go.presentation.settings.SettingsActivity
import dev.mainhq.bus2go.presentation.stop_direction.StopDirectionActivity
import dev.mainhq.bus2go.presentation.stop_times.StopTimesActivity
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import dev.mainhq.bus2go.utils.makeVisible
import dev.mainhq.bus2go.utils.launchViewModelCollectLatest
import dev.mainhq.bus2go.utils.makeGone

class HomeFragment: Fragment(R.layout.fragment_home) {

    private val homeFragmentViewModel: HomeFragmentViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeFragmentViewModel(
                    (this@HomeFragment.requireActivity().application as Bus2GoApplication).commonModule.getRouteInfo,
                    (this@HomeFragment.requireActivity().application as Bus2GoApplication).commonModule.getAllTags,
                ) as T
            }
        }
    }

    //uses more memory when outside of the fragment, but necessary if we want to keep the state
    // even outside of this specific fragment
    private val favouritesViewModel: FavouritesViewModel by viewModels {
        object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavouritesViewModel(
                    (this@HomeFragment.requireActivity().application as Bus2GoApplication).commonModule.getFavouritesWithTimeData,
                    (this@HomeFragment.requireActivity().application as Bus2GoApplication).commonModule.removeFavourite,
                    (this@HomeFragment.requireActivity().application as Bus2GoApplication).commonModule.moveFavourite,
                    (this@HomeFragment.requireActivity().application as Bus2GoApplication).commonModule.addTag,
                ) as T
            }
        }
    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    //FIXME move this var to the viewModel...
    private var wasSelectionMode = false

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

        binding.selectAllCheckbox.setOnClickListener{ favouritesViewModel.toggleSelectAllFavourites() }

        setFavourites(view)
        setSearchBar()
        setRemoveSelectionMode()

        binding.mainSearchBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.settingsIcon -> {
                    val intent = Intent(context, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    super.onOptionsItemSelected(menuItem)
                }
            }
        }

        binding.addTagWidget.setOnClickListener {
            //TODO show a dialog box with a text field
            //user enters the text
            //we dismiss the dialog, and create a new tag if it doesn't already exist
            //if it does, add the tag to the favourite if it doesn't have the existing tag
            val textInputLayout = TextInputLayout(requireContext())
            val editText = TextInputEditText(textInputLayout.context)
            //TODO
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add a tag")
                .setMessage("Coming Soon...")
                //.setView(editText)
                .setPositiveButton(""){ dialog, _ ->
                }
                .setNegativeButton("") { dialog, _ ->
                }
                .show()
        }

        binding.mainTagsRecyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val tagsAdapter = TagsListElemAdapter(homeFragmentViewModel.tags.value, null) {
            val button = it as MaterialButton
            homeFragmentViewModel.triggerFilterTagToFavouritesEvent(button.text.toString())
        }
        binding.mainTagsRecyclerview.adapter = tagsAdapter

        launchViewModelCollectLatest(homeFragmentViewModel.tags){ tags ->
            tagsAdapter.updateTags(tags)
            if (tags.isEmpty()) {
                binding.mainTagsRecyclerview.makeGone()
            }
            else {
                binding.mainTagsRecyclerview.makeVisible()
            }
        }

        launchViewModelCollectLatest(homeFragmentViewModel.tagSelected){ tagSelected ->
            tagsAdapter.updateSelectedTag(tagSelected)
        }

    }

    private fun setSearchBar(){
        //recyclerView for when searching
        val layoutManager = LinearLayoutManager(context)
        val busListAdapter = SearchQueryListElemsAdapter(ArrayList()){ data ->
            val intent = Intent(requireContext(), StopDirectionActivity::class.java)
            intent.putExtra(ExtrasTagNames.ROUTE_INFO, data)
            requireContext().startActivity(intent)
        }
        binding.searchRecycleView.adapter = busListAdapter
        binding.searchRecycleView.layoutManager = layoutManager

        launchViewModelCollectLatest(homeFragmentViewModel.searchQuery){ results ->
            when(results){
                is UiState.Error -> Log.e("DATABASE", "You have jack shit: ${results.message}")
                UiState.Loading -> throw IllegalStateException("Wtf")
                is UiState.Success<List<RouteInfo>> ->
                    busListAdapter.updateData(results.data)
                UiState.Init -> TODO()
            }
        }

        //setup the onBackPressedCallbacks
        val onBackPressedCallback = object : OnBackPressedCallback(false){
            override fun handleOnBackPressed() {
                homeFragmentViewModel.triggerBackPressed()
            }
        }

        /* Hide the bottom navigation bar when expanding the search bar to the search view */
        binding.mainSearchView.addTransitionListener { _, previousState, newState ->
            if (previousState == TransitionState.HIDDEN && newState == TransitionState.SHOWING){
                //can add an animation
                activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.makeGone()
                onBackPressedCallback.isEnabled = true
            }
            else if (previousState == TransitionState.SHOWN && newState == TransitionState.HIDING){
                activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.makeVisible()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        launchViewModelCollectLatest(homeFragmentViewModel.isBackPressed){
            if (binding.mainSearchView.currentTransitionState == TransitionState.SHOWN) {
                binding.mainSearchView.hide()
                onBackPressedCallback.isEnabled = false
            }
        }

        //handle the text input
        binding.mainSearchView.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(query: Editable?) {
                homeFragmentViewModel.onSearchQueryChange(query?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        binding.mainSearchView.editText.setOnEditorActionListener { textView : TextView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val submittedText = textView.text.toString()
                true
            }
            else {
                false
            }
        }
    }

    private fun setRemoveSelectionMode(){
        //during selection mode, listen to click on select all
        launchViewModelCollectLatest(favouritesViewModel.selectAllFavourites){ isChecked ->
            binding.selectAllCheckbox.isChecked = isChecked ?: false
        }

        //handling remove selection mode, coming from favourites fragment
        launchViewModelCollectLatest(homeFragmentViewModel.isSelectionModeActive){ removeFavouritesMode ->
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
    }

    private fun setFavourites(view: View) {
        val layoutManager = LinearLayoutManager(view.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val favouritesAdapter = FavouritesListElemsAdapter(
            listOf(),
            onClickListener = { itemView, favouriteTransitData -> //we are using the itemView of the holder
                if (favouritesViewModel.selectionMode.value) {
                    selectFavourite(itemView, favouriteTransitData)
                } else {
                    val intent = Intent(view.context, StopTimesActivity::class.java)
                    intent.putExtra(
                        ExtrasTagNames.TRANSIT_DATA,
                        FavouriteTransitData.fromFavouriteTransitDataToTransitData(
                            favouriteTransitData
                        )
                    )

                    itemView.context.startActivity(intent)
                    view.clearFocus()
                }
            },
            onLongClickListener = { itemView, favouriteTransitData ->
                if (!favouritesViewModel.selectionMode.value) {
                    favouritesViewModel.activateSelectionMode()
                    selectFavourite(itemView, favouriteTransitData)
                    true
                } else false
            },
            favouritesViewModel.favouritesToRemove.value
        )
        binding.favouritesRecyclerView.layoutManager = layoutManager
        binding.favouritesRecyclerView.adapter = favouritesAdapter
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                dragged: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                favouritesViewModel.moveFavourite(dragged.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }
        })
        itemTouchHelper.attachToRecyclerView(binding.favouritesRecyclerView)

        //This part allows us to press the back button when in selection mode of favourites to get out of it
        //we set the callback to false to prioritise it only when selection mode is activated
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            /** Hides all the checkboxes of the items in the recyclerview, deselects them, and puts back the searchbar as the nav bar */
            override fun handleOnBackPressed() {
                //TODO better way to do this/iterate through every item
                (0 until binding.favouritesRecyclerView.childCount).forEach { i ->

                    layoutManager.getChildAt(i)
                        ?.findViewById<MaterialCheckBox>(R.id.favourites_check_box)
                        ?.makeGone()
                }
                favouritesViewModel.deactivateSelectionMode()
                homeFragmentViewModel.deactivateSelectionMode()
                isEnabled = false
            }
        }

        //sets up top Favourites text and time remaining for each favourites
        launchViewModelCollectLatest(favouritesViewModel.favouriteDisplayTransitData) { uiState ->
            when(uiState){
                is UiState.Success<List<FavouritesDisplayModel>> -> {
                    if (uiState.data.isEmpty()) {
                        binding.favouritesTextView.text = getText(R.string.no_favourites)
                        favouritesAdapter.updateAdapter(uiState.data)
                    }
                    else {
                        binding.favouritesTextView.text = getText(R.string.favourites)
                        favouritesAdapter.updateAdapter(uiState.data)
                    }
                }
                UiState.Loading -> {}
                is UiState.Error -> Toast.makeText(context, "Some Unknown Error Occurred...", Toast.LENGTH_SHORT).show()
                UiState.Init -> Toast.makeText(context, "Some Error Occurred: Not Implemented", Toast.LENGTH_SHORT).show()
            }
        }

        launchViewModelCollectLatest(favouritesViewModel.favouritesToRemove){ favouritesToRemove  ->
            //change the appBar number displayed
            binding.selectedNumsOfFavourites.text = if (favouritesToRemove.isNotEmpty()) {
                binding.removeItemsWidget.makeVisible()
                binding.addTagWidget.makeVisible()
                favouritesToRemove.size.toString()
            }
            else {
                binding.removeItemsWidget.makeGone()
                binding.addTagWidget.makeGone()
                requireContext().getString(R.string.select_favourites_to_remove)
            }
            favouritesAdapter.toggleForRemoval(favouritesToRemove)
        }

        //updates recycler view adapter and top bar (search into selection mode and vice versa)
        launchViewModelCollectLatest(favouritesViewModel.selectionMode) { selectedMode ->
            if (wasSelectionMode != selectedMode){
                favouritesAdapter.updateSelectionMode()
                wasSelectionMode = selectedMode
                if (selectedMode) {
                    homeFragmentViewModel.activateSelectionMode()
                    onBackPressedCallback.isEnabled = true
                }
                else {
                    onBackPressedCallback.isEnabled = false
                }
            }
            //TODO more shit
        }

        //(de)select all favourites for removal
        launchViewModelCollectLatest(favouritesViewModel.selectAllFavourites) { isAllSelected ->
            if (favouritesViewModel.selectionMode.value){
                when (isAllSelected) {
                    true -> favouritesViewModel.selectAllForRemoval()
                    false -> favouritesViewModel.deselectAllForRemoval()
                    null -> {}
                }
            }
        }

        launchViewModelCollectLatest(homeFragmentViewModel.tagEvent){ event ->
            when(event) {
                is TagEvent.AddTagEvent -> TODO()
                is TagEvent.FilterFavouritesWithTagEvent -> {
                    favouritesViewModel.selectTag(event.tag)
                }
                is TagEvent.RemoveTagEvent -> TODO()
                TagEvent.RemoveTagFilter -> {
                    favouritesViewModel.unselectTag()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        binding.removeItemsWidget.setOnClickListener { _ ->
            this.context?.also { context ->
                MaterialAlertDialogBuilder(context)
                    .setTitle("Remove Selected Favourites?")
                    .setMessage(resources.getString(R.string.remove_confirmation_dialog_text))
                    .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .setPositiveButton(resources.getString(R.string.remove_confirmation_dialog_accept)) { dialog, _ ->
                        //TODO
                        favouritesViewModel.removeFavourites()
                        favouritesAdapter.removeSelected()
                        when(val uiState = favouritesViewModel.favouriteDisplayTransitData.value){
                            is UiState.Success<List<FavouritesDisplayModel>> -> {
                                if (uiState.data.size == favouritesViewModel.favouritesToRemove.value.size)
                                    homeFragmentViewModel.deactivateSelectionMode()
                                dialog.dismiss()
                            }
                            UiState.Loading -> {}
                            is UiState.Error -> throw object : Bus2GoBaseException("Wtf") {}
                            UiState.Init -> TODO()
                        }
                    }
                    .show()
            }
        }
    }

    private fun selectFavourite(itemView: View, favouriteTransitData: FavouriteTransitData){
        val checkBoxView = itemView.findViewById<MaterialCheckBox>(R.id.favourites_check_box)
        favouritesViewModel.toggleFavouriteForRemoval(favouriteTransitData)
        checkBoxView.isChecked = favouritesViewModel.favouritesToRemove.value.contains(favouriteTransitData)
    }

    //FIXME keep the last state
    // for now, avoids to make the bottomNav disappear...
    override fun onPause() {
        super.onPause()
        binding.mainSearchBar.makeVisible()
        binding.selectionModeBar.makeGone()
        activity?.findViewById<CoordinatorLayout>(R.id.bottomNavCoordLayout)?.makeVisible()
    }

    override fun onDestroyView() {
        onBackPressedCallback.remove()
        super.onDestroyView()
        _binding = null
    }

}