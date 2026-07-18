package dev.mainhq.bus2go.domain.entity

import kotlin.jvm.Throws

enum class DbToDownload(private val str: String) {
	STM("Stm"),
	EXO("Exo");

	override fun toString() = str

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