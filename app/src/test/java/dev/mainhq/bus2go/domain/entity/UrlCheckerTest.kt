package dev.mainhq.bus2go.domain.entity

import org.junit.Test

class UrlCheckerTest {

	@Test
	fun `Test Build Empty String Expect Null`(){
		assert(UrlChecker.check("") == null)
	}

	//Ip Addresses
	@Test
	fun `Test Build Empty Ip Expect Null`(){
		assert(UrlChecker.check("....") == null)
	}

	@Test
	fun `Test Build Missing Number Ip Expect Null`(){
		val num = 1
		val str = "..."
		(0..str.length).forEach {
			val str1 = str.substring(0 until it)
			val str2 = str.substring(it until str.length)
			val copy = str1 + num + str2
			println(copy)
			assert(UrlChecker.check(copy) == null)
		}
	}

	@Test
	fun `Test Build Wrong Number Ip Format Expect Null`(){
		val nums = listOf(
			"10.21.232.1230",
			"12.21.1230.232",
			"12.1230.21.232",
			"2123.12.01.232",
		)
		nums.forEach{ assert(UrlChecker.check(it) == null) }

	}

	@Test
	fun `Test Build Wrong Ip3 Format Expect Null`(){
		assert(UrlChecker.check("1....") == null)
	}

	@Test
	fun `Test Build Wrong Ip4 Format Expect Null`(){
		assert(UrlChecker.check("1....") == null)
	}

	//Domain Names

}