package dev.mainhq.bus2go.domain.entity

enum class DbToDownload(str: String) {
	//ALL("All"), //TODO get rid of all, and use lists in business logic instead...
	STM("Stm"),
	EXO("Exo");

	fun isAll(count: Int): Boolean {
		return count == entries.size
	}
}