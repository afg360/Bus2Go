package dev.mainhq.bus2go.presentation.stop_direction

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.mainhq.bus2go.Bus2GoApplication
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.presentation.stop_direction.direction.DirectionFragment
import dev.mainhq.bus2go.presentation.stop_direction.direction.DirectionFragmentViewModel
import dev.mainhq.bus2go.presentation.stop_direction.stop.StopFragment
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import dev.mainhq.bus2go.utils.launchViewModelCollect

class StopDirectionActivity: BaseActivity() {

	private val viewModel: StopDirectionViewModel by viewModels()
	private val directionSharedViewModel: DirectionFragmentViewModel by viewModels {
		object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				if (modelClass.isAssignableFrom(DirectionFragmentViewModel::class.java)) {
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

		val routeInfo = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.extras?.getParcelable(ExtrasTagNames.ROUTE_INFO, RouteInfo::class.java)
		} else {
			intent.extras?.getParcelable(ExtrasTagNames.ROUTE_INFO)
		}) ?: throw IllegalStateException("Expected a non null RouteInfo passed")
		directionSharedViewModel.setRouteInfo(routeInfo)

		launchViewModelCollect(viewModel.animationDirection) {
			supportFragmentManager.beginTransaction().apply {
				when (it) {
					AnimationDirection.TO_TOP -> {
						setCustomAnimations(
							R.anim.enter_from_bottom,
							R.anim.exit_to_top,
						).replace(
							R.id.stop_direction_fragment_container_view,
							StopFragment()
						)
					}

					AnimationDirection.TO_BOTTOM -> {
						setCustomAnimations(
							R.anim.enter_from_top,
							R.anim.exit_to_bottom,
						).replace(
							R.id.stop_direction_fragment_container_view,
							StopFragment()
						)
					}

					AnimationDirection.FROM_TOP -> {
						setCustomAnimations(
							R.anim.enter_from_top,
							R.anim.exit_to_bottom
						).replace(
							R.id.stop_direction_fragment_container_view,
							DirectionFragment()
						)
					}

					AnimationDirection.FROM_BOTTOM -> {
						setCustomAnimations(
							R.anim.enter_from_bottom,
							R.anim.exit_to_top
						).replace(
							R.id.stop_direction_fragment_container_view,
							DirectionFragment()
						)
					}

					AnimationDirection.FROM_TIMES_ACTIVITY -> {
						if (viewModel.previousAnimationDirection.value == null) {
							replace(
								R.id.stop_direction_fragment_container_view,
								StopFragment()
							)
						}
					}

					null -> {
						replace(
							R.id.stop_direction_fragment_container_view,
							DirectionFragment()
						)
					}

				}
			}.commit()
		}
	}
}