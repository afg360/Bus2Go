package dev.mainhq.bus2go

import dev.mainhq.bus2go.utils.Time
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime

class TimeClassTest {

    @Test
    fun testSubNoSecs() {
        val prev = Time(LocalDateTime.of(2025, 3, 5, 2, 25, 0))
        val new = Time(LocalDateTime.of(2025, 3, 5, 4, 0, 0))
        //we expect the duration to be of 01:35:00
        assertEquals(LocalTime.parse("01:35"), new - prev)
    }

    @Test
    fun testSubSecs() {
        val prev = Time(LocalDateTime.of(2025, 3, 5, 2, 25, 23))
        val new = Time(LocalDateTime.of(2025, 3, 5, 4, 0, 0))
        //we expect the duration to be of 01:35:00
        assertEquals(LocalTime.parse("01:34:37"), new - prev)
    }

    @Test
    fun testInitLate() {
        val time = Time.fromString("26:21:23")
        assertEquals(
            Time(LocalDate.now().plusDays(1), LocalTime.of(2, 21, 23)).toString(),
            time.toString()
        )
    }

    @Test
    fun testInitLateAndSub1() {
        val time1 = Time.fromString("25:21:23")
        val time2 = Time.fromString("26:21:23")
        assertEquals(LocalTime.of(1, 0, 0), time2 - time1)
    }

    @Test
    fun testInitLateAndSub2() {
        val time1 = Time.fromString("26:21:23")
        val time2 = Time.fromString("21:23:21")
        assertEquals(LocalTime.of(4, 58, 2), time1 - time2)
    }

    @Test
    fun testInitLateAndSubExpectNull() {
        val time1 = Time.fromString("26:21:23")
        val time2 = Time.fromString("21:23:21")
        assertEquals(null, time2 - time1)
    }

    @Test
    fun testInitUnix() {
        val unix = Time.fromUnix(1741231534)
        assertEquals(Time(LocalDateTime.of(2025, 3, 5, 22, 25, 34)), unix)
    }

    @Test
    fun testDayString() {
        val times = mutableListOf<Time>()
        val daysOfWeek = listOf("d", "m", "t", "w", "y", "f", "s")
        //from march 2 to 8 2025, we go from Sunday to Saturday (passing by Mon
        for (i in 2..8) {
            times.add(Time(LocalDate.of(2025, 3, i), LocalTime.now()))
        }
        times.zip(daysOfWeek) { time, str ->
            assertEquals(time.getDayString(), str)
        }
    }

    @Test
    fun testTimeString() {
        val time = Time(LocalTime.of(2, 32, 12))
        assertEquals("02:32:12", time.getTimeString())
    }

    @Test
    fun testTodayString() {
        val time = Time(LocalDate.of(2025, 3, 6), LocalTime.now())
        assertEquals("20250306", time.getTodayString())
    }
}
