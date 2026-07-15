package dev.mainhq.bus2go.domain.entity

import kotlin.jvm.Throws

enum class DbToDownload(private val str: String) {
	//ALL("All"), //TODO get rid of all, and use lists in business logic instead...
	STM("Stm"),
	EXO("Exo");

	override fun toString() = str

	fun isAll(count: Int): Boolean {
		return count == entries.size
	}

	companion object {
		/** @throws IllegalArgumentException When an invalid name is given to be searched for */
		@Throws(IllegalArgumentException::class)
		fun getEntry(name: String): DbToDownload {
			return when(name.lowercase()) {
				"stm" -> STM
				"exo" -> EXO
				else -> throw IllegalArgumentException("Entry $name does not exist as a possible DbToDownload")
			}
		}
	}
}