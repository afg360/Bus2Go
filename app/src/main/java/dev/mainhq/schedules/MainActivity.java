package dev.mainhq.schedules;

import dev.mainhq.schedules.utils.Parser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Objects;

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
        this.listenSearchQuery();
        this.listenSettings();
    }
    //could put this in a generic class
    private void listenSearchQuery(){
        SearchView searchView = this.findViewById(R.id.search_widget);
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
                if (newText != null){
                    Log.d("TEXT", newText);
                    return true;
                }
                return false; // Return true if the query has been handled, false otherwise.
            }
        });
    }
    private void listenSettings(){
        View view = this.findViewById(R.id.settings);
        view.setOnClickListener(tmpView -> {
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
        });
    }
}