package dev.mainhq.bus2go.presentation.stop_direction.stop

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.databinding.FragmentChooseStopBinding
import dev.mainhq.bus2go.domain.entity.compareTransitData
import dev.mainhq.bus2go.presentation.stop_direction.ActivityFragment
import dev.mainhq.bus2go.presentation.stop_direction.AnimationDirection
import dev.mainhq.bus2go.presentation.stop_direction.StopDirectionViewModel
import dev.mainhq.bus2go.presentation.stop_times.StopTimesActivity
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import dev.mainhq.bus2go.utils.launchViewModelCollect

//todo
//instead of doing a huge query on getting the time, we could first retrieve
//all the possible stations (ordered by id, and prob based on localisation? -> not privacy friendly
//and once the user clicks, either new activity OR new fragment? -> in the latter case need to implement onback
//todo add possibility of searching amongst all the stops
class StopFragment : Fragment(R.layout.fragment_choose_stop) {

    private val viewModel : StopFragmentViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return (this@StopFragment.requireActivity().application as Bus2GoApplication)
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

    private val sharedActivityViewModel: StopDirectionViewModel by activityViewModels()

    private var _binding: FragmentChooseStopBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentChooseStopBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = StopListElemsAdapter(
            viewModel.stopNames.value,
            viewModel.favourites.value,
            //TODO update the little star
            toggleFavouritesClickListener = { view, innerTransitData ->
                if (viewModel.favourites.value.compareTransitData(innerTransitData)){
                    viewModel.removeFavourite(innerTransitData)
                    view.findViewById<ImageView>(R.id.favourite_star_selection)
                        .setBackgroundResource(R.drawable.favourite_drawable_off)
                }
                else {
                    viewModel.addFavourite(innerTransitData)
                    view.findViewById<ImageView>(R.id.favourite_star_selection)
                        .setBackgroundResource(R.drawable.favourite_drawable_on)
                }
            },
            onClickListener = {
                sharedActivityViewModel.toTimesActivity()
                val intent = Intent(requireContext(), StopTimesActivity::class.java)
                //FIXME send another class model instead
                intent.putExtra(ExtrasTagNames.TRANSIT_DATA, it)
                startActivity(intent)
            }
        )
        binding.stopRecycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.stopRecycleView.adapter = adapter

        launchViewModelCollect(viewModel.stopNames){
            adapter.updateTransitData(it)
            if (it.isEmpty()) {
                Toast.makeText(requireContext(), "No stops found...", Toast.LENGTH_SHORT).show()
            }
        }
        launchViewModelCollect(viewModel.favourites){
            adapter.updateFavourites(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (sharedActivityViewModel.previousAnimationDirection.value != null){
                        val cachedValue = sharedActivityViewModel.previousAnimationDirection.value
                        sharedActivityViewModel.resetCache()
                        when(cachedValue){
                            AnimationDirection.TO_TOP -> sharedActivityViewModel.setAnimationDirection(AnimationDirection.FROM_TOP)
                            AnimationDirection.TO_BOTTOM -> sharedActivityViewModel.setAnimationDirection(AnimationDirection.FROM_BOTTOM)
                            else -> {
                                Toast.makeText(requireContext(), "Wtf...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else {
                        when (sharedActivityViewModel.animationDirection.value) {
                            AnimationDirection.TO_TOP -> sharedActivityViewModel.setAnimationDirection(
                                AnimationDirection.FROM_TOP
                            )

                            AnimationDirection.TO_BOTTOM -> sharedActivityViewModel.setAnimationDirection(
                                AnimationDirection.FROM_BOTTOM
                            )

                            else -> {
                                Toast.makeText(requireContext(), "Wtf...", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    isEnabled = false
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}