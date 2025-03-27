package dev.mainhq.bus2go.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


//breaks pure clean architecture..
@Parcelize
/**
 * Entities used for getting route information for buses. When passing from search
 * to direction choice activity
 * */
sealed class RouteInfo : Parcelable {
	abstract val routeId : String
	abstract val routeName : String
}

@Parcelize
data class StmBusRouteInfo(override val routeId: String, override val routeName: String)
	: RouteInfo()

@Parcelize
data class ExoBusRouteInfo(override val routeId: String, override val routeName: String)
	: RouteInfo()

@Parcelize
data class ExoTrainRouteInfo(override val routeId: String, override val routeName: String, val trainNum: Int)
	: RouteInfo()
