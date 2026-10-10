package app.bus2go.android.presentation.stop_direction.direction;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.bus2go.android.R
import app.bus2go.android.domain.entity.ExoBusItem
import app.bus2go.android.domain.entity.ExoBusRouteInfo
import app.bus2go.android.domain.entity.ExoTrainItem
import app.bus2go.android.domain.entity.ExoTrainRouteInfo
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.domain.entity.StmBusItem
import app.bus2go.android.domain.entity.StmBusRouteInfo
import app.bus2go.android.Bus2GoApplication
import app.bus2go.android.databinding.FragmentChooseDirectionBinding
import app.bus2go.android.presentation.core.UiState
import app.bus2go.android.presentation.stop_direction.AnimationDirection
import app.bus2go.android.presentation.stop_direction.StopDirectionViewModel
import app.bus2go.android.presentation.stop_direction.stop.StopFragmentViewModel
import app.bus2go.android.utils.getMaterialColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.IllegalStateException
import app.bus2go.android.utils.launchViewModelCollectLatest
import app.bus2go.android.utils.makeGone
import app.bus2go.android.utils.makeVisible
import app.bus2go.android.utils.toast
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class DirectionFragment : Fragment() {
    private val viewModel: DirectionFragmentViewModel by activityViewModels{
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DirectionFragmentViewModel::class.java)){
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
    ): View {
        _binding = FragmentChooseDirectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
		//TODO set a loading screen first before displaying the correct buttons

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewModel.cardViewColour.filterNotNull().first().also{
                //DO NOT USE setBackgroundColor SINCE IT IS GOING TO FUCK UP MaterialCardView style
                //TODO add a tiny bit of a tint of the main color to give it a material look
                binding.chooseBusBadgeCard.strokeColor = getMaterialColor(it)
                binding.chooseBusNum.setTextColor(getMaterialColor(it))
                binding.topRouteCardView.strokeColor = getMaterialColor(it)
                binding.bottomRouteCardView.strokeColor = getMaterialColor(it)
            }
        }

        launchViewModelCollectLatest(viewModel.routeInfo){
            when(it){
                is UiState.Error -> toast(it.message)
                UiState.Init -> {}
                UiState.Loading -> {}
                is UiState.Success<RouteInfo> -> {
                    binding.chooseBusNum.text = it.data.routeId
                    binding.chooseBusDir.text = it.data.routeName
                }
            }
        }

        launchViewModelCollectLatest(viewModel.routeInfo) {
            val isUnidirectional = viewModel.isUnidirectional.filterNotNull().first()

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
                            sharedActivityViewModel.setAnimationDirection(
                                //ActivityFragment.Stops(AnimationDirection.TO_TOP)
                                AnimationDirection.TO_TOP
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
                                sharedActivityViewModel.setAnimationDirection(
                                    //ActivityFragment.Stops(AnimationDirection.TO_TOP)
                                    AnimationDirection.TO_TOP
                                )
                            }
                            binding.bottomRouteCardView.setOnClickListener {
                                sharedStopViewModel.setTransitData(bottomDirData)
                                sharedActivityViewModel.setAnimationDirection(
                                    //ActivityFragment.Stops(AnimationDirection.TO_BOTTOM)
                                    AnimationDirection.TO_BOTTOM
                                )
                            }
                        }
                    }
                    is ExoTrainRouteInfo -> {
                        val topDirData = viewModel.topDirection.filterNotNull().first() as List<ExoTrainItem>
                        if (!isUnidirectional) {
                            val bottomDirData = viewModel.bottomDirection.filterNotNull()
                                .first() as List<ExoTrainItem>

                            binding.topRouteDirection.makeGone()
                            binding.topRouteDescription.text = getString(
                                R.string.from_to,
                                topDirData.first().stopName,
                                topDirData.last().stopName
                            )
                            binding.topRouteCardView.setOnClickListener {
                                sharedStopViewModel.setTransitData(topDirData)
                                sharedActivityViewModel.setAnimationDirection(
                                    //ActivityFragment.Stops(AnimationDirection.TO_TOP)
                                    AnimationDirection.TO_TOP
                                )
                            }

                            binding.bottomRouteDirection.makeGone()
                            binding.bottomRouteDescription.text = getString(
                                R.string.from_to,
                                bottomDirData.first().stopName,
                                bottomDirData.last().stopName
                            )
                            binding.bottomRouteCardView.setOnClickListener {
                                sharedStopViewModel.setTransitData(bottomDirData)
                                sharedActivityViewModel.setAnimationDirection(
                                    AnimationDirection.TO_BOTTOM
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
                                sharedActivityViewModel.setAnimationDirection(
                                    //ActivityFragment.Stops(AnimationDirection.TO_TOP)
                                    AnimationDirection.TO_TOP
                                )
                            }

                            binding.bottomRouteDescription.text = getString(
                                R.string.from_to,
                                bottomDirData.first().stopName,
                                bottomDirData.last().stopName
                            )
                            binding.bottomRouteCardView.setOnClickListener {
                                sharedStopViewModel.setTransitData(bottomDirData)
                                sharedActivityViewModel.setAnimationDirection(
                                    //ActivityFragment.Stops(AnimationDirection.TO_BOTTOM)
                                    AnimationDirection.TO_BOTTOM
                                )
                            }
                        }
                        else throw IllegalStateException("Unexpected data: StmBus being unidirectional...")
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
