package dev.mainhq.bus2go.domain

import org.junit.Test

class RandomTest {

	@Test
	fun foo(){
		val db = "exo_data"
		val version = 1
		val COMPRESSION_EXT = "rar"
		val list = listOf(
			"foo", "bar", "${db}_${version}.db.$COMPRESSION_EXT"
		)
		assert(list.any { it.matches( "${db}_[0-9]+.db.${COMPRESSION_EXT}".toRegex()) })
	}
}