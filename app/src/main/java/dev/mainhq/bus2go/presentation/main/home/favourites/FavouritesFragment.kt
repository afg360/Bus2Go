package dev.mainhq.bus2go.presentation.main.home.favourites

import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.TransitData
import dev.mainhq.bus2go.presentation.Bus2GoApplication
import dev.mainhq.bus2go.presentation.stopTimes.StopTimesActivity
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavouritesFragment: Fragment(R.layout.fragment_favourites) {

    companion object{
        const val NOT_CONNECTED = "Not connected to the internet yet"

    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: NetworkCallback? = null

    //FIXME move this var to the viewModel...
    private var wasSelectionMode = false

    //FIXME use some sort of global state holder to cache the number of favourites currently saved
    // so that "No favourites made yet" doesn't get displayed unnecessarily...

    private val favouritesViewModel: FavouritesViewModel by viewModels{
        object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(FavouritesViewModel::class.java)){
                    return FavouritesViewModel(
                        (this@FavouritesFragment.requireActivity().application as Bus2GoApplication).appContainer.favouritesUseCases,
                    ) as T
                }
                throw IllegalArgumentException("Gave wrong ViewModel class")
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
            favouritesViewModel.favouriteTransitData.value,
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
                    true
                }
                else false
            },
            favouritesViewModel.favouritesToRemove.value
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        //sets up top Favourites text and time remaining for each favourites
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favouritesViewModel.favouriteTransitData.collect { favouritesList ->
                    //inside the main thread by default, no need for withContext(Dispatchers.Main)
                    if (favouritesList.isEmpty()) {
                        view.findViewById<MaterialTextView>(R.id.favourites_text_view).text =
                            getText(R.string.no_favourites)
                    }
                    else {
                        view.findViewById<MaterialTextView>(R.id.favourites_text_view).text =
                            getText(R.string.favourites)
                        adapter.updateTime(favouritesList)
                        /*
						//check if connected to internet
						connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
						if (connectivityManager?.activeNetwork == null){
							Log.d(Companion::NOT_CONNECTED.name, NOT_CONNECTED)
							lifecycleScope.launch {
								recyclerViewDisplay(view, favouritesList, new=true)
							}
						}
						val networkRequest = NetworkRequest.Builder()
							.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
							.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
							//.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
							.build()

						networkCallback = object: NetworkCallback() {
							// do your websocketing shit here, not outside of it
							override fun onAvailable(network: Network) {
								super.onAvailable(network)
								println("A network became available")
								//do some work in a thread?
								lifecycleScope.launch {
									if (isRealtimeEnabled){
										isUsingRealTime = true
										//getRealTime will never return if the connection is alright since it is an infinite loop
										//need to deal with it with perhaps an exception
										if (realTimeViewModel.getRealTime(listSTM) == 1) {
											Toast.makeText( context, "An error occured trying to connect to the bus2go server",
												Toast.LENGTH_SHORT
											).show()
										}
									}
									else
										recyclerViewDisplay(view, mutableList, new=true)
								}
							}

							// Network capabilities have changed for the network
							override fun onCapabilitiesChanged( network: Network, networkCapabilities: NetworkCapabilities ) {
								super.onCapabilitiesChanged(network, networkCapabilities)
								val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
								//println("Network Capabilities have changed")
							}

							// lost network connection
							override fun onLost(network: Network) {
								super.onLost(network)
								println("Lost connection to network")
								isUsingRealTime = false
								//tmp solution for now. needs to adapt to data already received by server if it exists
								lifecycleScope.launch {
									recyclerViewDisplay(view, mutableList, new=true)
								}
							}

							override fun onUnavailable() {
								super.onUnavailable()
								println("Networks are unavailable")
								isUsingRealTime = false
								lifecycleScope.launch {
									recyclerViewDisplay(view, mutableList, new=true)
								}
							}
						}
						connectivityManager!!.registerNetworkCallback(networkRequest, networkCallback!!)
					}
						 */
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                favouritesViewModel.favouritesToRemove.collect { favouritesToRemove ->
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favouritesSharedViewModel.selectAllFavourites.collect { isAllSelected ->
                    if (favouritesViewModel.selectionMode.value){
                        when (isAllSelected) {
                            true -> {
                                favouritesViewModel.selectAllForRemoval()
                                favouritesSharedViewModel.setAllFavouritesSelected(favouritesViewModel.favouriteTransitData.value.size)
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


        /** This part allows us to press the back button when in selection mode of favourites to get out of it */
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            /** Hides all the checkboxes of the items in the recyclerview, deselects them, and puts back the searchbar as the nav bar */
            override fun handleOnBackPressed() {
                recyclerView.forEach { view ->
                    view.findViewById<MaterialCheckBox>(R.id.favourites_check_box).visibility = View.GONE
                }
                favouritesViewModel.deactivateSelectionMode()
                favouritesSharedViewModel.deactivateSelectionMode()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)

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
                            favouritesViewModel.removeFavourites()
                            favouritesSharedViewModel.resetNumFavouritesSelected()
                            dialog.dismiss()
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
        favouritesSharedViewModel.toggleIsAllSelected(favouritesViewModel.favouritesToRemove.value.size == favouritesViewModel.favouriteTransitData.value.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.remove()
        networkCallback?.also{
            connectivityManager?.unregisterNetworkCallback(it)
        }
    }

}
