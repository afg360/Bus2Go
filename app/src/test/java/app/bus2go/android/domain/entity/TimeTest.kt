package app.bus2go.android.domain.entity

import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class TimeTest {

    /**
     * Creates a new Time object from some [LocalTime] string. The date will be set to today.
     * @param dateTime Must be of format YYYY:MM:DD:HH:MM:SS
     * @throws IllegalArgumentException if wrong input format for [dateTime].
     **/
    private fun fromDateTimeString(dateTime : String): Time {
        val list : List<String> = dateTime.split(":")
        try {
            return if (list[3].toInt() > 23){
                Time(LocalDateTime.of(list[0].toInt(), list[1].toInt(), list[2].toInt(), list[3].toInt() % 24, list[4].toInt(), list[5].toInt()))
            }
            else if (list[0].toInt() < 4) {
                Time(LocalDateTime.of(list[0].toInt(), list[1].toInt(), list[2].toInt() - 1, list[3].toInt() % 24, list[4].toInt(), list[5].toInt()))
            }
            else Time(LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy:MM:dd:HH:mm:ss")))
        }
        catch (ie : IndexOutOfBoundsException){
            throw IllegalArgumentException("The input str must be of the form HH:MM:SS")
        }
    }

    @Test
    fun testInitLateAndSub1() {
        val time1 = Time.fromTimeString("25:21:23")
        val time2 = Time.fromTimeString("26:21:23")
        assertEquals(Duration.of(1, ChronoUnit.HOURS), time2 - time1)
    }

    @Test
    fun testInitLateAndSub2() {
        val time1 = Time.fromTimeString("26:21:23")
        val time2 = Time.fromTimeString("21:23:21")
        assertEquals(Duration.ofSeconds(4 * 3600 + 58 * 60 + 2), time1 - time2)
    }

    @Test
    fun testInitLateAndSubExpectNull() {
        val time1 = Time.fromTimeString("26:21:23")
        val time2 = Time.fromTimeString("21:23:21")
        assertEquals(null, time2 - time1)
    }

    @Test
    fun testBeforeMidnightForBusesAfterMidnight() {
        val time1 = Time.fromTimeString("23:59:00")
        val time2 = Time.fromTimeString("26:59:00")
        assertEquals(Duration.ofHours(3), time2 - time1)
    }

    @Test
    fun testBeforeMidnightForBusesBeforeMidnight() {
        val time1 = Time.fromTimeString("23:00:00")
        val time2 = Time.fromTimeString("23:59:00")
        assertEquals(Duration.ofMinutes(59), time2 - time1)
    }

    @Test
    fun testBeforeMidnightForBusesAfterMidnightAm() {
        val time1 = Time.fromTimeString("23:00:00")
        //2 am of today == 26h of yesterday
        val time2 = Time.fromTimeString("02:00:00")
        assertEquals(null, time2 - time1)
    }

    @Test
    fun testAfterMidnightForBusesAfterMidnight() {
        val time1 = Time.fromTimeString("26:00:00")
        val time2 = Time.fromTimeString("26:59:00")
        assertEquals(Duration.ofMinutes(59), time2 - time1)
    }

    @Test
    fun testAfterMidnightInAMForBusesAfterMidnight() {
        //2 am of today === 26h of yesterday which is < 26h of today
        val time1 = Time.fromTimeString("02:00:00")
        val time2 = Time.fromTimeString("26:59:00")
        assertEquals(Duration.ofDays(1).plusMinutes(59), time2 - time1)
    }

    @Test
    fun testAfterMidnightInForBusesAfterMidnightAM() {
        //should be considered the same hour
        val time1 = Time.fromTimeString("26:00:00")
        //2 am of today === 26h of yesterday which is < 26h of today
        val time2 = Time.fromTimeString("02:59:00")
        assertEquals(null, time2 - time1)
    }

    @Test
    fun testQueryDayStringCorrectness() {
        //supposed to be a Sunday
        val queryDay = fromDateTimeString("2026:09:13:26:59:00").getQueryDayString()
        assertEquals("d", queryDay)
    }

    @Test
    fun testQueryDayString() {
        val queryDay = fromDateTimeString("2026:05:03:02:59:00")
        val queryDayExpected = fromDateTimeString("2026:05:03:23:00:00")
        assertEquals(queryDayExpected.getQueryDayString(), queryDay.getQueryDayString())
    }

    @Test
    fun testQueryDayStringAfterMidnight() {
        val queryDate = fromDateTimeString("2026:09:13:26:59:00")
        val queryDateExpected = fromDateTimeString("2026:09:13:15:00:00")
        assertEquals(queryDateExpected.getQueryDayString(), queryDate.getQueryDayString())
    }

    @Test
    fun testQueryDayStringAfterMidnight2() {
        val queryDate = fromDateTimeString("2026:05:03:26:59:00")
        val queryDateExpected = fromDateTimeString("2026:05:03:02:00:00")
        assertEquals(queryDateExpected.getQueryDayString(), queryDate.getQueryDayString())
    }

    @Test
    fun testTodayString() {
        val time = Time.fromMillis(LocalDateTime.of(LocalDate.of(2025, 3, 6), LocalTime.now()).toEpochSecond(ZoneOffset.ofHours(0)) * 1000L)
        assertEquals("20250306", time.getTodayString())
    }
}
