package dev.mainhq.bus2go.domain.entity

//Used internally for making it easier to decipher lists of data
enum class TransitType(private val str: String) {
	STM("Stm"),
	EXO_BUS("Exo_Bus"),
	EXO_TRAIN("Exo_Train");

	override fun toString() = str

	fun toDatabaseAgency(): DatabaseAgency {
		return when(this) {
			STM -> DatabaseAgency.STM
			EXO_BUS -> DatabaseAgency.EXO
			EXO_TRAIN -> DatabaseAgency.EXO
		}
	}

	companion object {
		/** @throws IllegalArgumentException When an invalid name is given to be searched for */
		@Throws(IllegalArgumentException::class)
		fun getEntry(name: String): TransitType {
			return when(name.lowercase()) {
				"stm" -> STM
				"exo_bus" -> EXO_BUS
				"exo_train" -> EXO_TRAIN
				else -> throw IllegalArgumentException("Entry $name does not exist as a possible DbToDownload")
			}
		}
	}
}