package dev.mainhq.bus2go.domain.entity

/**
 * Entity used for getting route information for buses. When passing from search
 * to direction choice activity
 * */
data class BusRouteInfo(val routeId : String, val routeName : String)
