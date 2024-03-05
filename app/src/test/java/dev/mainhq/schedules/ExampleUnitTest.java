package dev.mainhq.schedules;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Hashtable;
import dev.mainhq.schedules.MainActivity;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void checkTable(){
        String[] test = {"test", "test1"};
        Hashtable<String[], String> tmp = new Hashtable<>();
        tmp.put(test, "value of test");
        assertEquals("value of test", tmp.get(test));
    }
}