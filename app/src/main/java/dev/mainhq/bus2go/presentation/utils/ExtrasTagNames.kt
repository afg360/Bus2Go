package dev.mainhq.bus2go.presentation.utils

/** Enum class listing multiple strings used inside bundles between activities */
object ExtrasTagNames {
                         //new shit
    const val ROUTE_INFO = "ROUTE_INFO" //used in MainActivity
    const val TRANSIT_DATA = "TRANSIT_DATA" //used inside Times activity

    const val ROUTE_NAME = "ROUTE_NAME" // = "BUS_NAME"

    const val QUERY = "QUERY" //used to send the query from HomeFragment to SearchActivity

    const val BUS_NUM = "BUS_NUM"
    const val AGENCY = "AGENCY"
    const val DIRECTION = "DIRECTION"
    const val HEADSIGN = "HEADSIGN"
    /** Only for use with trains! */
    /** Actual route id, as listed in the .txt files */
    const val ROUTE_ID = "ROUTE_ID"
    /** The number in the train route_long_name, e.g. %11% - blablabla */
    const val TRAIN_NUM = "TRAIN_NUM"
    const val DIRECTION_ID = "DIRECTION_ID"
    /** For stm buses, the last stop */
    const val LAST_STOP = "LAST_STOP"
}