package dev.mainhq.bus2go.domain.entity

import kotlin.jvm.Throws

enum class DatabaseAgency(private val str: String) {
	STM("Stm"),
	EXO("Exo");

	override fun toString() = str

	/** Does not contain the database file extension. */
	fun toDatabaseFileNamePrefixString() = "${toString().lowercase()}_data"

	companion object {
		/** @throws IllegalArgumentException When an invalid name is given to be searched for */
		@Throws(IllegalArgumentException::class)
		fun getEntry(name: String): DatabaseAgency {
			return when(name.lowercase()) {
				"stm" -> STM
				"exo" -> EXO
				else -> throw IllegalArgumentException("Entry $name does not exist as a possible DbToDownload")
			}
		}
	}
}