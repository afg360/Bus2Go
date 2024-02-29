package dev.mainhq.schedules;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    boolean submitted;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (false){//check if config file exists
            Intent intent = new Intent(this.getApplicationContext(), Config.class);
            startActivity(intent);
        }
        this.setContentView(R.layout.main_activity);
        SearchView searchView = findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !submitted){
                    Log.d("Query:", query);
                    submitted = true;
                    Intent intent = new Intent(getApplicationContext(), Search.class);
                    intent.putExtra("query", query);
                    startActivity(intent);
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
