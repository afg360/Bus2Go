package dev.mainhq.bus2go.domain.entity

//Used internally for making it easier to decipher lists of data
enum class TransitType(str: String) {
	STM("Stm"),
	EXO_BUS("Exo_Bus"),
	EXO_TRAIN("Exo_Train")
}