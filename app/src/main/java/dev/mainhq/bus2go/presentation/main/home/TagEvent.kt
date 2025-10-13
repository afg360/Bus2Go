package dev.mainhq.bus2go.presentation.main.home

sealed class TagEvent(open val tag: String) {
	data class AddTagEvent(override val tag: String): TagEvent(tag)
	//an empty string will represent the "null"
	data class FilterFavouritesWithTagEvent(override val tag: String): TagEvent(tag)
	data object RemoveTagFilter: TagEvent("")
	data class RemoveTagEvent(override val tag: String): TagEvent(tag)
}