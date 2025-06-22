package dev.mainhq.bus2go.presentation.stop_direction

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.presentation.stop_direction.direction.DirectionFragment
import dev.mainhq.bus2go.presentation.stop_direction.direction.DirectionFragmentViewModel
import dev.mainhq.bus2go.presentation.stop_direction.stop.StopFragment
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StopDirectionActivity: BaseActivity() {

	private val viewModel: StopDirectionViewModel by viewModels()
	private val directionSharedViewModel: DirectionFragmentViewModel by viewModels {
		object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				if (modelClass.isAssignableFrom(DirectionFragmentViewModel::class.java)){
					return (application as Bus2GoApplication).let {
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.stop_direction_activity)

		lifecycleScope.launch(Dispatchers.Main) {
			repeatOnLifecycle(Lifecycle.State.STARTED){
				viewModel.activityFragment.collect {
					when(it){
						ActivityFragment.Direction -> {
							supportFragmentManager.beginTransaction()
								.replace(
									R.id.stop_direction_fragment_container_view,
									DirectionFragment()
								)
								.commit()
						}
						is ActivityFragment.Stops -> {
							supportFragmentManager.beginTransaction()
								.apply{
									when(it.animationDirection){
										AnimationDirection.TO_TOP -> {
											setCustomAnimations(
												R.anim.enter_from_bottom,
												R.anim.exit_to_top,
												R.anim.enter_from_top,
												R.anim.exit_to_bottom
											)
										}
										AnimationDirection.TO_BOTTOM -> {
											setCustomAnimations(
												R.anim.enter_from_top,
												R.anim.exit_to_bottom,
												R.anim.enter_from_bottom,
												R.anim.exit_to_top
											)
										}
									}
								}
								.replace(
									R.id.stop_direction_fragment_container_view,
									StopFragment()
								)
								.commit()
						}
					}
				}
			}
		}

		lifecycleScope.launch(Dispatchers.Main) {
			//terminus (i.e. to destination) = data.last(), needed for exo because some of the headsigns are the same

			val routeInfo = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
				intent.extras?.getParcelable(ExtrasTagNames.ROUTE_INFO, RouteInfo::class.java)
			else {
				@Suppress("DEPRECATION")
				intent.extras?.getParcelable(ExtrasTagNames.ROUTE_INFO)
			}) ?: throw IllegalStateException("Expected a non null RouteInfo passed")

			directionSharedViewModel.setRouteInfo(routeInfo)
		}

		val backDispatch = object: OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				TODO("Not yet implemented")
			}

		}
	}
}