package dev.mainhq.bus2go;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Hashtable;

/**
 * Example local unit M_outline, which will execute on the development machine (host).
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
        String[] test = {"M_outline", "test1"};
        Hashtable<String[], String> tmp = new Hashtable<>();
        tmp.put(test, "value of M_outline");
        assertEquals("value of M_outline", tmp.get(test));
    }
}