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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.presentation.stopTimes.StopTimesActivity
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavouritesFragment: Fragment(R.layout.fragment_favourites) {

    companion object{
        const val NOT_CONNECTED = "Not connected to the internet yet"
    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: NetworkCallback? = null

    private val favouritesViewModel: FavouritesViewModel by viewModels()
    private val favouritesSharedViewModel: FavouritesFragmentSharedViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val realTimeViewModel: RealTimeViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.favouritesRecyclerView)
        val layoutManager = LinearLayoutManager(view.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val adapter = FavouritesListElemsAdapter(
            listOf(),
            { itemView, favouriteTransitData -> //we are using the itemView of the holder
                if (favouritesViewModel.selectionMode.value){
                    val checkBoxView = itemView as MaterialCheckBox
                    checkBoxView.isChecked = !checkBoxView.isChecked
                }
                else {
                    val intent = Intent(view.context, StopTimesActivity::class.java)
                    intent.putExtra(ExtrasTagNames.TRANSIT_DATA.name, favouriteTransitData)

                    itemView.context.startActivity(intent)
                    view.clearFocus()
                }
            },
            {
                favouritesViewModel.activateSelectionMode()
                favouritesSharedViewModel.activateSelectionMode()
                true
            }
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                favouritesSharedViewModel.selectAllFavourites.collect { isAllSelected ->
                    if (isAllSelected == true){
                        favouritesViewModel.selectAllForRemoval()
                    }
                    else if (isAllSelected == false) {
                        favouritesViewModel.deselectAllForRemoval()
                    }
                }
            }

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favouritesViewModel.selectionMode.collect { selectedMode ->
                    favouritesSharedViewModel.setSelectionMode(selectedMode)
                    //TODO more shit
                }
            }

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favouritesViewModel.favouriteTransitData.collect { favouritesList ->
                    if (favouritesList.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            view.findViewById<MaterialTextView>(R.id.favourites_text_view).text =
                                getText(R.string.no_favourites)
                        }
                    }
                    else {
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

        /** This part allows us to press the back button when in selection mode of favourites to get out of it */
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            /** Hides all the checkboxes of the items in the recyclerview, deselects them, and puts back the searchbar as the nav bar */
            override fun handleOnBackPressed() {
                recyclerView.forEach { view ->
                    view.findViewById<MaterialCheckBox>(R.id.favourites_check_box).visibility = View.GONE
                    /** Establish original layout (i.e. margins) */
                    setMargins(view.findViewById(R.id.favouritesDataContainer), 20, 20)
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
                            favouritesViewModel.removeFavourites()
                            favouritesSharedViewModel.deactivateSelectionMode()
                            dialog.dismiss()
                        }
                        .show()
                }
            }
    }
    

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.remove()
        networkCallback?.also{
            connectivityManager?.unregisterNetworkCallback(it)
        }
    }

}
