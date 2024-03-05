package dev.mainhq.schedules;

import dev.mainhq.schedules.utils.Parser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
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
        InputStream inputStream = Parser.makeInputStream(this,routesFile);
        Hashtable<String[], String> busInfo = Parser.readTextFileFromAssets(inputStream);

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
}
