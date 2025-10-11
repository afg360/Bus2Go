package dev.mainhq.bus2go.presentation.stop_direction.direction;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.databinding.FragmentChooseDirectionBinding
import dev.mainhq.bus2go.presentation.core.UiState
import dev.mainhq.bus2go.presentation.stop_direction.ActivityFragment
import dev.mainhq.bus2go.presentation.stop_direction.AnimationDirection
import dev.mainhq.bus2go.presentation.stop_direction.StopDirectionViewModel
import dev.mainhq.bus2go.presentation.stop_direction.stop.StopFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.IllegalStateException
import dev.mainhq.bus2go.utils.launchViewModelCollect
import dev.mainhq.bus2go.utils.makeVisible
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

//TODO
//change appbar to be only a back button
//TODO may make it a swappable ui instead of choosing button0 or 1
class DirectionFragment : Fragment() {
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

    private var _binding: FragmentChooseDirectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChooseDirectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
		//TODO set a loading screen first before displaying the correct buttons

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            binding.chooseBusNum.setTextColor(resources.getColor(viewModel.textColour.filterNotNull().first(), null))
            viewModel.cardViewColour.filterNotNull().first().also{
                //DO NOT USE setBackgroundColor SINCE IT IS GOING TO FUCK UP MaterialCardView style
                binding.topRouteCardView.setCardBackgroundColor(resources.getColor(it, null))
                binding.bottomRouteCardView.setCardBackgroundColor(resources.getColor(it, null))
            }
        }

        launchViewModelCollect(viewModel.routeInfo){
            when(it){
                is UiState.Error -> TODO()
                UiState.Init -> TODO()
                UiState.Loading -> {}
                is UiState.Success<RouteInfo> -> {
                    binding.chooseBusNum.text = it.data.routeId
                    binding.chooseBusDir.text = it.data.routeName
                }
            }
        }

        //viewLifecycleOwner.launchViewModelCollect()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                                    sharedActivityViewModel.setActivityFragment(
                                        ActivityFragment.Stops(AnimationDirection.TO_TOP)
                                    )
                                }
                                else {
                                    val bottomDirData = viewModel.bottomDirection.filterNotNull()
                                        .first() as List<ExoBusItem>
                                    assert(bottomDirData.isNotEmpty())

                                    binding.topRouteDirection.visibility = View.GONE
                                    binding.topRouteDescription.text = getString(
                                        R.string.from_to,
                                        topDirData.first().stopName,
                                        topDirData.last().stopName
                                    )

                                    binding.bottomRouteDirection.visibility = View.GONE
                                    binding.bottomRouteDescription.text = getString(
                                        R.string.from_to,
                                        bottomDirData.first().stopName,
                                        bottomDirData.last().stopName
                                    )
                                    binding.topRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(topDirData)
                                        sharedActivityViewModel.setActivityFragment(
                                            ActivityFragment.Stops(AnimationDirection.TO_TOP)
                                        )
                                    }
                                    binding.bottomRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(bottomDirData)
                                        sharedActivityViewModel.setActivityFragment(
                                            ActivityFragment.Stops(AnimationDirection.TO_BOTTOM)
                                        )
                                    }
                                }
                            }
                            is ExoTrainRouteInfo -> {
                                val topDirData = viewModel.topDirection.filterNotNull().first() as List<ExoTrainItem>
                                if (!isUnidirectional) {
                                    val bottomDirData = viewModel.bottomDirection.filterNotNull()
                                        .first() as List<ExoTrainItem>

                                    binding.topRouteDirection.visibility = View.GONE
                                    binding.topRouteDescription.text = getString(
                                        R.string.from_to,
                                        topDirData.first().stopName,
                                        topDirData.last().stopName
                                    )
                                    binding.topRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(topDirData)
                                        sharedActivityViewModel.setActivityFragment(
                                            ActivityFragment.Stops(AnimationDirection.TO_TOP)
                                        )
                                    }

                                    binding.bottomRouteDirection.visibility = View.GONE
                                    binding.bottomRouteDescription.text = getString(
                                        R.string.from_to,
                                        bottomDirData.first().stopName,
                                        bottomDirData.last().stopName
                                    )
                                    binding.bottomRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(bottomDirData)
                                        sharedActivityViewModel.setActivityFragment(
                                            ActivityFragment.Stops(AnimationDirection.TO_BOTTOM)
                                        )
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

                                    binding.topRouteDirection.makeVisible()
                                    binding.bottomRouteDirection.makeVisible()

                                    //top thing must always be East or South
                                    if (topDirData.first().direction.lowercase() == "est") {
                                        binding.topRouteDirection.text = getString(R.string.west)
                                        binding.bottomRouteDirection.text = getString(R.string.east)
                                        val tmpData = topDirData
                                        topDirData = bottomDirData
                                        bottomDirData = tmpData
                                    }
                                    else if (topDirData.first().direction.lowercase() == "ouest") {
                                        binding.topRouteDirection.text = getString(R.string.west)
                                        binding.bottomRouteDirection.text = getString(R.string.east)
                                    }
                                    else if (topDirData.first().direction.lowercase() == "north") {
                                        binding.topRouteDirection.text  = getString(R.string.north)
                                        binding.bottomRouteDirection.text = getString(R.string.south)
                                    }
                                    else {
                                        binding.topRouteDirection.text = getString(R.string.north)
                                        binding.bottomRouteDirection.text = getString(R.string.south)
                                        val tmpData = topDirData
                                        topDirData = bottomDirData
                                        bottomDirData = tmpData
                                    }
                                    binding.topRouteDescription.text = getString(
                                        R.string.from_to,
                                        topDirData.first().stopName,
                                        topDirData.last().stopName
                                    )
                                    binding.topRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(topDirData)
                                        sharedActivityViewModel.setActivityFragment(
                                            ActivityFragment.Stops(AnimationDirection.TO_TOP)
                                        )
                                    }

                                    binding.bottomRouteDescription.text = getString(
                                        R.string.from_to,
                                        bottomDirData.first().stopName,
                                        bottomDirData.last().stopName
                                    )
                                    binding.bottomRouteCardView.setOnClickListener {
                                        sharedStopViewModel.setTransitData(bottomDirData)
                                        sharedActivityViewModel.setActivityFragment(
                                            ActivityFragment.Stops(AnimationDirection.TO_BOTTOM)
                                        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
