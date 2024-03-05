package dev.mainhq.schedules;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (false){//check if config file exists
            Intent intent = new Intent(this.getApplicationContext(), Config.class);
            startActivity(intent);
        }
        String routesFile = "stm_data_info/routes.txt";
        Hashtable<String[], String> busInfo = readTextFileFromAssets(routesFile);
        Log.d("DEBUG", busInfo.toString());
        this.setContentView(R.layout.main_activity);
        this.setSupportActionBar(this.findViewById(R.id.toolbar));
        SearchView searchView = findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null){
                    Log.d("Query:", query);
                    Intent intent = new Intent(getApplicationContext(), Search.class);
                    intent.putExtra("query", query);
                    startActivity(intent);
                    searchView.clearFocus();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // Return true if the query has been handled, false otherwise.
            }
        });
    }

    private Hashtable<String[], String> readTextFileFromAssets(String fileName) {
        Hashtable<String[], String> list = new Hashtable<>();
        try {
            InputStream inputStream = this.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            //should skip first 5 lines
            while (line != null) {
                //could color depending on express, night or normal
                String[] vals = line.split(",");
                String num = vals[0];
                String name = vals[3];
                name = toParsable(name);
                //map modified string to real string
                list.put(new String[] {num, name}, vals[3]);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            inputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static String toParsable(String str){
        str = str.toLowerCase();
        str = str.replace("'", "");
        str = str.replace("-", "");
        str = str.replace(" ", "");
        str = str.replace("/", "");
        str = str.replace("é", "e");
        str = str.replace("è", "e");
        str = str.replace("ê", "e");
        str = str.replace("ç", "c");
        str = str.replace("î", "i");
        str = str.replace("ô", "o");
        str = str.replace("û", "u");
        return str;
    }
}
