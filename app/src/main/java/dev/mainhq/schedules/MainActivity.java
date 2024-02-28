package dev.mainhq.schedules;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
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
                if (query != null){
                    Log.d("Query:", query);
                    return true;
                }
                return false; // Return true if the query has been handled, false otherwise.
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // This method is called whenever the text in the search field changes.
                // newText contains the new text entered by the user.
                // Here you can perform actions such as filtering data based on the new query.
                return false; // Return true if the query has been handled, false otherwise.
            }
        });
    }
}
