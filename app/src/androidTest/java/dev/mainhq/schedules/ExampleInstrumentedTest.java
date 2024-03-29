package dev.mainhq.schedules;

import android.content.Context;
import android.widget.SearchView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.InputStream;

import dev.mainhq.schedules.utils.Parser;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("dev.mainhq.schedules", appContext.getPackageName());
    }
    @Test
    public void checkFileOpening(){
        ActivityScenario.launch(MainActivity.class).onActivity(
                activity -> {
                    InputStream inputStream = Parser.makeInputStream(activity, "stm_data_info/routes.txt");
                    assertNotNull(Parser.readBusRoutesFromAssets(inputStream));
        });
    }
    @Test
    public void checkQuery(){
        String query1 = "mon";
        ActivityScenario.launch(MainActivity.class).onActivity(
                activity ->{
                    SearchView sv = activity.findViewById(R.id.app_bar_search_icon);
                    sv.setQuery(query1, true);
                    assertEquals(query1, sv.getQuery().toString());
                }
        );
        //Espresso.
    }

}