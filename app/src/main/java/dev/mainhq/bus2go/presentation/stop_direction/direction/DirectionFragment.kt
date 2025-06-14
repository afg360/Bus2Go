package dev.mainhq.bus2go.presentation.stop_direction.direction;

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.presentation.core.UiState
import dev.mainhq.bus2go.presentation.stop_direction.ActivityFragment
import dev.mainhq.bus2go.presentation.stop_direction.StopDirectionSharedViewModel
import dev.mainhq.bus2go.presentation.stop_direction.StopDirectionViewModel
import dev.mainhq.bus2go.presentation.stop_direction.stop.StopFragment
import dev.mainhq.bus2go.presentation.stop_direction.stop.StopFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.IllegalStateException
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

//TODO
//change appbar to be only a back button
//TODO may make it a swappable ui instead of choosing button0 or 1
class DirectionFragment : Fragment(R.layout.fragment_choose_direction) {
    private val viewModel: DirectionFragmentViewModel by activityViewModels{
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DirectionFragmentViewModel::class.java)){
                    return (this@DirectionFragment.requireActivity().application as Bus2GoApplication).let {
                        DirectionFragmentViewModel(
                            getDirections = it.commonModule.getDirections,
                            getStopNames = it.commonModule.getStopNames,
                        ) as T
                    }
                }
                throw IllegalArgumentException("Gave wrong ViewModel class")
            }
        }
    }

    private val sharedActivityViewModel: StopDirectionViewModel by activityViewModels()

    private val sharedStopViewModel: StopFragmentViewModel by activityViewModels{
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return (this@DirectionFragment.requireActivity().application as Bus2GoApplication)
                    .let {
                        StopFragmentViewModel(
                            addFavourite = it.commonModule.addFavourite,
                            removeFavourite = it.commonModule.removeFavourite,
                            getFavourites = it.commonModule.getFavourites,
                        ) as T
                    }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

		//TODO set a loading screen first before displaying the correct buttons

        val busNumView = view.findViewById<MaterialTextView>(R.id.choose_bus_num)
        val busNameView = view.findViewById<MaterialTextView>(R.id.choose_bus_dir)
        val topRouteCardView = view.findViewById<MaterialCardView>(R.id.top_route_card_view)
        val bottomRouteCardView = view.findViewById<MaterialCardView>(R.id.bottom_route_card_view)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            busNumView.setTextColor(resources.getColor(viewModel.textColour.filterNotNull().first(), null))
            viewModel.cardViewColour.filterNotNull().first().also{
                //DO NOT USE setBackgroundColor SINCE IT IS GOING TO FUCK UP MaterialCardView style
                topRouteCardView.setCardBackgroundColor(resources.getColor(it, null))
                bottomRouteCardView.setCardBackgroundColor(resources.getColor(it, null))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.routeInfo.collect{
                    when(it){
                        is UiState.Error -> TODO()
                        UiState.Init -> TODO()
                        UiState.Loading -> {}
                        is UiState.Success<RouteInfo> -> {
                            busNumView.text = it.data.routeId
                            busNameView.text = it.data.routeName
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val topDir = topRouteCardView.findViewById<MaterialTextView>(R.id.top_route_direction)
                val topDescr  = topRouteCardView.findViewById<MaterialTextView>(R.id.top_route_description)
                val bottomDir = bottomRouteCardView.findViewById<MaterialTextView>(R.id.bottom_route_direction)
                val bottomDescr = bottomRouteCardView.findViewById<MaterialTextView>(R.id.bottom_route_description)

                val isUnidirectional = viewModel.isUnidirectional.filterNotNull().first()

                //we are using collect since routeInfo may still be loading...
                viewModel.routeInfo.collect {
                    if (it is UiState.Success<RouteInfo>) {
                        when(it.data){
                            is ExoBusRouteInfo -> {
                                val topDirData = viewModel.topDirection.filterNotNull().first() as List<ExoBusItem>
                                assert(topDirData.isNotEmpty())

                                if (isUnidirectional) {
                                    Toast.makeText(
                                        this@DirectionFragment.requireContext(),
                                        "This bus line only contains 1 direction",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    sharedStopViewModel.setTransitData(topDirData)
                                    sharedActivityViewModel.setActivityFragment(ActivityFragment.STOPS)
                                }
                                else {
                                    val bottomDirData = viewModel.bottomDirection.filterNotNull()
                                        .first() as List<ExoBusItem>
                                    assert(bottomDirData.isNotEmpty())

                                    topDir.visibility = View.GONE
                                    topDescr.text = getString(
                                        R.string.from_to,
                                        topDirData.first().stopName,
                                        topDirData.last().stopName
                                    )

                                    bottomDir.visibility = View.GONE
                                    bottomDescr.text = getString(
                                        R.string.from_to,
                                        bottomDirData.first().stopName,
                                        bottomDirData.last().stopName
                                    )
                                    topRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(topDirData)
                                        sharedActivityViewModel.setActivityFragment(ActivityFragment.STOPS)
                                    }
                                    bottomRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(bottomDirData)
                                        sharedActivityViewModel.setActivityFragment(ActivityFragment.STOPS)
                                    }
                                }
                            }
                            is ExoTrainRouteInfo -> {
                                val topDirData = viewModel.topDirection.filterNotNull().first() as List<ExoTrainItem>
                                if (!isUnidirectional) {
                                    val bottomDirData = viewModel.bottomDirection.filterNotNull()
                                        .first() as List<ExoTrainItem>

                                    topDir.visibility = View.GONE
                                    topDescr.text = getString(
                                        R.string.from_to,
                                        topDirData.first().stopName,
                                        topDirData.last().stopName
                                    )
                                    topRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(topDirData)
                                        sharedActivityViewModel.setActivityFragment(ActivityFragment.STOPS)
                                    }

                                    bottomDir.visibility = View.GONE
                                    bottomDescr.text = getString(
                                        R.string.from_to,
                                        bottomDirData.first().stopName,
                                        bottomDirData.last().stopName
                                    )
                                    bottomRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(bottomDirData)
                                        sharedActivityViewModel.setActivityFragment(ActivityFragment.STOPS)
                                    }
                                }
                                else throw IllegalStateException("Unexpected data: ExoTrain being unidirectional...")
                            }
                            is StmBusRouteInfo -> {
                                var topDirData =
                                    viewModel.topDirection.filterNotNull().first() as List<StmBusItem>
                                if (!isUnidirectional) {
                                    var bottomDirData = viewModel.bottomDirection.filterNotNull()
                                        .first() as List<StmBusItem>

                                    topDir.visibility = View.VISIBLE
                                    bottomDir.visibility = View.VISIBLE

                                    //top thing must always be East or South
                                    if (topDirData.first().direction.lowercase() == "est") {
                                        topDir.text = getString(R.string.west)
                                        bottomDir.text = getString(R.string.east)
                                        val tmpData = topDirData
                                        topDirData = bottomDirData
                                        bottomDirData = tmpData
                                    }
                                    else if (topDirData.first().direction.lowercase() == "ouest") {
                                        topDir.text = getString(R.string.west)
                                        bottomDir.text = getString(R.string.east)
                                    }
                                    else if (topDirData.first().direction.lowercase() == "north") {
                                        topDir.text = getString(R.string.north)
                                        bottomDir.text = getString(R.string.south)
                                    }
                                    else {
                                        topDir.text = getString(R.string.north)
                                        bottomDir.text = getString(R.string.south)
                                        val tmpData = topDirData
                                        topDirData = bottomDirData
                                        bottomDirData = tmpData
                                    }
                                    topDescr.text = getString(
                                        R.string.from_to,
                                        topDirData.first().stopName,
                                        topDirData.last().stopName
                                    )
                                    topRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(topDirData)
                                        sharedActivityViewModel.setActivityFragment(ActivityFragment.STOPS)
                                    }

                                    bottomDescr.text = getString(
                                        R.string.from_to,
                                        bottomDirData.first().stopName,
                                        bottomDirData.last().stopName
                                    )
                                    bottomRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(bottomDirData)
                                        sharedActivityViewModel.setActivityFragment(ActivityFragment.STOPS)
                                    }
                                }
                                else throw IllegalStateException("Unexpected data: StmBus being unidirectional...")
                            }
                        }
                    }
                }
            }
        }
    }
}
