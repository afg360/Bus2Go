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

	@Test
	fun bar(){
		val db = "stm"
		val version = 4
		val COMPRESSION_EXT = "gz"
		val list = listOf(
			"${db}_sample_data_${version-1}.db.$COMPRESSION_EXT",
			"${db}_data_${version}.db.$COMPRESSION_EXT",
			"${db}_data_${version*26}.db.$COMPRESSION_EXT"
		)
		assert(list.all { it.matches("^(stm|exo)(_sample)?_data_\\d+.db.gz$".toRegex()) })
	}

	@Test
	fun shit() {
		val db = "stm"
		val version = 4
		val COMPRESSION_EXT = "gz"
		val realVersionNum = version * 26
		val fileName = "${db}_data_${realVersionNum}.db.$COMPRESSION_EXT"
		//function to test
		val versionNum = fileName.split(".")[0].split("_").last().toInt()
		assert(versionNum == realVersionNum)
	}
}