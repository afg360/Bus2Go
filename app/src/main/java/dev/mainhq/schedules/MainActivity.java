package dev.mainhq.schedules;

import dev.mainhq.schedules.utils.Parser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (false){//check if config file exists
            Intent intent = new Intent(this.getApplicationContext(), Config.class);
            startActivity(intent);
        }
        String routesFile = "stm_data_info/routes.txt";

        this.setContentView(R.layout.main_activity);
        //this.listenSearchQuery();
        //this.listenSettings();
        //this.chooseBus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search_icon);
        SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        listenSearchQuery(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemID = item.getItemId();
        if (itemID == R.id.settingsIcon) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }

    //could put this in a generic class
    private void listenSearchQuery(SearchView searchView){
        //SearchView searchView = this.findViewById(R.id.search_widget);
        searchView.setQueryHint("Search for bus lines, bus nums, etc.");

        //searchView.
        AppCompatActivity curr = this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null){
                    Log.d("Query:", query);
                    Intent intent = new Intent(getApplicationContext(), SearchBus.class);
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
                    ArrayList<String[]> dataQueried = Parser.setup(curr, Parser.DataType.busList, newText);
                    //if null, display "Sorry, no matches made"
                    if (dataQueried == null) {
                        String empty = "No Matches Found";
                    }
                    else{
                        RecyclerView recyclerView = findViewById(R.id.search_recycle_view);
                        recyclerView.setBackgroundColor(getResources().getColor(R.color.white, null));
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(new BusListElemsAdapter(dataQueried));
                        searchView.addTouchables(recyclerView.getTouchables());
                    }
                }
                //need to handle when it is null
                //set the search widget to display "search for buses, wtv"
                else{
                    Log.d("EMPTY STRING", "Empty search bar");
                }
                return true;
            }
        });
    }
    public void chooseBus(){
        View view = findViewById(R.id.bus_elem);
        if (view != null) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChooseBus.class);
                startActivity(intent);
            });
        }
    }
}