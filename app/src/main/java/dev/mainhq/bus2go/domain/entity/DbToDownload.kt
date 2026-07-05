package dev.mainhq.bus2go.domain.entity

enum class DbToDownload(private val str: String) {
	//ALL("All"), //TODO get rid of all, and use lists in business logic instead...
	STM("Stm"),
	EXO("Exo");

	override fun toString() = str

	fun isAll(count: Int): Boolean {
		return count == entries.size
	}
}