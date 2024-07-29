package dev.mainhq.bus2go

import dev.mainhq.bus2go.utils.Time
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TimeClassTest {
    @Test
    fun testInit(){
        val time = Time(0,0,0)
        assertEquals("00:00:00", time.toString())
    }
    @Test
    fun testInit2(){
        val time = Time(0, 6, 13)
        assertEquals("00:06:13", time.toString())
    }
    @Test
    fun testSub1(){
        val time1 = Time(5,2,1)
        val time2 = Time(2,1,0)
        assertEquals(Time(3,1,1), time1.subtract(time2))
    }
    @Test
    fun testSub2(){
        val time1 = Time(5,2,1)
        val time2 = Time(2,3,0)
        assertEquals(Time(2,59,1), time1.subtract(time2))
    }
    @Test
    fun testSub3(){
        val time1 = Time(3,2,1)
        val time2 = Time(2,3,23)
        assertEquals(Time(0,58,38), time1.subtract(time2))
    }
    @Test
    fun testSubErr(){
        val time1 = Time(3,2,1)
        val time2 = Time(2,3,23)
        assertTrue(time2.subtract(time1) == null)
    }
}