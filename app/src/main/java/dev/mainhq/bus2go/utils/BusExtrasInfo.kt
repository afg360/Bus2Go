package dev.mainhq.bus2go.utils

/** Enum class listing multiple strings used inside bundles between activities */
enum class BusExtrasInfo {
    ROUTE_NAME, // = "BUS_NAME"
    BUS_NUM,
    AGENCY,
    DIRECTION,
    
    /** Only for use with trains! */
    /** Actual route id, as listed in the .txt files */
    ROUTE_ID,
    /** The number in the train route_long_name, e.g. %11% - blablabla */
    TRAIN_NUM,
    DIRECTION_ID,
    /** For stm buses, the last stop */
    LAST_STOP
}