package app.bus2go.android.presentation.stop_direction

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.bus2go.android.Bus2GoApplication
import app.bus2go.android.R
import app.bus2go.android.domain.entity.RouteInfo
import app.bus2go.android.presentation.base.BaseActivity
import app.bus2go.android.presentation.stop_direction.direction.DirectionFragment
import app.bus2go.android.presentation.stop_direction.direction.DirectionFragmentViewModel
import app.bus2go.android.presentation.stop_direction.stop.StopFragment
import app.bus2go.android.presentation.utils.ExtrasTagNames
import app.bus2go.android.utils.launchViewModelCollectLatest

class StopDirectionActivity: BaseActivity() {

	private val viewModel: StopDirectionViewModel by viewModels()
	private val directionSharedViewModel: DirectionFragmentViewModel by viewModels {
		object : ViewModelProvider.Factory {
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				if (modelClass.isAssignableFrom(DirectionFragmentViewModel::class.java)) {
					@Suppress("UNCHECKED_CAST")
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

		launchViewModelCollectLatest(viewModel.animationDirection) {
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