package dev.mainhq.bus2go.presentation.main.home.favourites

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.domain.exceptions.Bus2GoBaseException
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.presentation.core.UiState
import dev.mainhq.bus2go.presentation.stop_times.StopTimesActivity
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch


class FavouritesFragment: Fragment(R.layout.fragment_favourites) {

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    //FIXME move this var to the viewModel...
    private var wasSelectionMode = false

    //FIXME use some sort of global state holder to cache the number of favourites currently saved
    // so that "No favourites made yet" doesn't get displayed unnecessarily...

    //FIXME to follow strict clean architecture, event posting should be done outside of UI...

    private val favouritesViewModel: FavouritesViewModel by viewModels{
        object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavouritesViewModel(
                    (this@FavouritesFragment.requireActivity().application as Bus2GoApplication).commonModule.getFavouritesWithTimeData,
                    (this@FavouritesFragment.requireActivity().application as Bus2GoApplication).commonModule.removeFavourite,
                ) as T
            }
        }
    }
    private val favouritesSharedViewModel: FavouritesFragmentSharedViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    //private val realTimeViewModel: RealTimeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.favouritesRecyclerView)
        val layoutManager = LinearLayoutManager(view.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val adapter = FavouritesListElemsAdapter(
            listOf(),
            onClickListener = { itemView, favouriteTransitData -> //we are using the itemView of the holder
                if (favouritesViewModel.selectionMode.value){
                    selectFavourite(itemView, favouriteTransitData)
                }
                else {
                    val intent = Intent(view.context, StopTimesActivity::class.java)
                    intent.putExtra(ExtrasTagNames.TRANSIT_DATA, favouriteTransitData)

                    itemView.context.startActivity(intent)
                    view.clearFocus()
                }
            },
            onLongClickListener = { itemView, favouriteTransitData ->
                if (!favouritesViewModel.selectionMode.value) {
                    favouritesViewModel.activateSelectionMode()
                    selectFavourite(itemView, favouriteTransitData)
                    onBackPressedCallback.isEnabled = true
                    true
                }
                else false
            },
            favouritesViewModel.favouritesToRemove.value
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        //This part allows us to press the back button when in selection mode of favourites to get out of it
        //we set the callback to false to prioritise it only when selection mode is activated
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            /** Hides all the checkboxes of the items in the recyclerview, deselects them, and puts back the searchbar as the nav bar */
            override fun handleOnBackPressed() {
                (0 until recyclerView.childCount).forEach { i ->
                    layoutManager.getChildAt(i)?.let { view ->
                        view.findViewById<MaterialCheckBox>(R.id.favourites_check_box).visibility = View.GONE
                    }
                }
                favouritesViewModel.deactivateSelectionMode()
                favouritesSharedViewModel.deactivateSelectionMode()
                onBackPressedCallback.isEnabled = false
            }
        }

        //sets up top Favourites text and time remaining for each favourites
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favouritesViewModel.favouriteTransitData.filterNotNull().collect { uiState ->
                    when(uiState){
                        is UiState.Success<List<FavouritesDisplayModel>> -> {
                            if (uiState.data.isEmpty()) {
                                view.findViewById<MaterialTextView>(R.id.favourites_text_view).text =
                                    getText(R.string.no_favourites)
                            }
                            else {
                                view.findViewById<MaterialTextView>(R.id.favourites_text_view).text =
                                    getText(R.string.favourites)
                                adapter.updateTime(uiState.data)
                            }
                        }
                        UiState.Loading -> {}
                        is UiState.Error -> throw object: Bus2GoBaseException("Wtf"){}
                        UiState.Init -> TODO()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                favouritesViewModel.favouritesToRemove.filterNotNull().collect { favouritesToRemove ->
                    adapter.toggleForRemoval(favouritesToRemove)
                }
            }
        }

        //updates recycler view adapter and top bar (search into selection mode and vice versa)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favouritesViewModel.selectionMode.collect { selectedMode ->
                    if (wasSelectionMode != selectedMode){
                        adapter.updateSelectionMode()
                        wasSelectionMode = selectedMode
                        if (selectedMode)
                            favouritesSharedViewModel.activateSelectionMode()
                    }
                    //TODO more shit
                }
            }
        }

        //(de)select all favourites for removal
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favouritesSharedViewModel.selectAllFavourites.collect { isAllSelected ->
                    if (favouritesViewModel.selectionMode.value){
                        when (isAllSelected) {
                            true -> {
                                favouritesViewModel.selectAllForRemoval()
                                when(val uiState = favouritesViewModel.favouriteTransitData.value){
                                    is UiState.Success<List<FavouritesDisplayModel>> -> {
                                        favouritesSharedViewModel.setAllFavouritesSelected(uiState.data.size)
                                    }
                                    UiState.Loading -> {}
                                    is UiState.Error -> throw  object : Bus2GoBaseException("Wtf"){}
                                    UiState.Init -> TODO()
                                }
                            }
                            false -> {
                                favouritesViewModel.deselectAllForRemoval()
                                favouritesSharedViewModel.resetNumFavouritesSelected()
                            }
                            null -> {}
                        }
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        parentFragment?.view?.findViewById<LinearLayout>(R.id.removeItemsWidget)
            ?.setOnClickListener { _ ->
                this.context?.also { context ->
                    MaterialAlertDialogBuilder(context)
                        //.setTitle(resources.getString(R.string.title))
                        .setMessage(resources.getString(R.string.remove_confirmation_dialog_text))
                        .setNegativeButton(resources.getString(R.string.remove_confirmation_dialog_decline)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .setPositiveButton(resources.getString(R.string.remove_confirmation_dialog_accept)) { dialog, _ ->
                            adapter.removeSelected()
                            favouritesSharedViewModel.resetNumFavouritesSelected()
                            when(val uiState = favouritesViewModel.favouriteTransitData.value){
                                is UiState.Success<List<FavouritesDisplayModel>> -> {
                                    if (uiState.data.size == favouritesViewModel.favouritesToRemove.value.size)
                                        favouritesSharedViewModel.deactivateSelectionMode()
                                    favouritesViewModel.removeFavourites()
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

    private fun selectFavourite(itemView: View, favouriteTransitData: TransitData){
        val checkBoxView = itemView.findViewById<MaterialCheckBox>(R.id.favourites_check_box)
        checkBoxView.isChecked = !checkBoxView.isChecked
        if (checkBoxView.isChecked) favouritesSharedViewModel.incrementNumFavouritesSelected()
        else favouritesSharedViewModel.decrementNumFavouritesSelected()
        favouritesViewModel.toggleFavouriteForRemoval(favouriteTransitData)
        when(val uiState = favouritesViewModel.favouriteTransitData.value){
            is UiState.Success<List<FavouritesDisplayModel>> -> {
                favouritesSharedViewModel.toggleIsAllSelected(
                    favouritesViewModel.favouritesToRemove.value.size == uiState.data.size
                )
            }
            UiState.Loading -> {}
            is UiState.Error -> throw object : Bus2GoBaseException("Wtf") {}
            UiState.Init -> TODO()
        }

    }
}
