package dev.mainhq.schedules;

import dev.mainhq.schedules.utils.Parser;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.lang.String;
import java.lang.Character;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;

public class Search extends AppCompatActivity {
    //processed query
    private String query;
    //we tmp removed busInfo hashtable gotten from main
    //bcz doesnt work yet so duplicate code...
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.search);
        this.setSupportActionBar(this.findViewById(R.id.toolbar));
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.query = extras.getString("query");
            String routesFile = "stm_data_info/routes.txt";
            ArrayList<String[]> dataQueried = setup(routesFile);
            //if null, display "Sorry, no matches made"
            //if (dataQueried == null) //todo;
            //else
            RecyclerView recyclerView = this.findViewById(R.id.search_recycle_view);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(new BusListElemsAdapter(dataQueried));
        }
        else Log.d("Query", "None");
    }

    public void onClickPrev(View view){
        this.finish();
    }

    @Nullable
    private ArrayList<String[]> setup(String filepath){
        InputStream inputStream = Parser.makeInputStream(this, filepath);
        Hashtable<String[], String> busInfo = Parser.readTextFileFromAssets(inputStream);
        if (this.query != null){
            QueryType type = getType(this.query);
            Set<String[]> busInfoKeys = busInfo.keySet();
            ArrayList<String[]> list = new ArrayList<>(0);
            switch(type){
                //if query is in first part of the actual name\
                //also consider!
                case NUM_ONLY:
                    boolean found = false;
                    for (String[] arr : busInfoKeys){
                        if (arr[0].contains(this.query)) {
                            Log.d("FOUND VAL", Objects.requireNonNull(busInfo.get(arr))
                                    + "\nBus: " + arr[0]);
                            list.add(new String[]{arr[0], busInfo.get(arr)});
                            found = true;
                        }
                    }
                    if (!found) Log.e("Mistype", "coudlnt find res for query");
                    break;
                case ALPHA_ONLY:
                    boolean pres = false;
                    for (String[] arr : busInfoKeys){
                        //for now print all possible combinations
                        //must also work if mispell (using dicts?, regex?)
                        if (arr[1].contains(this.query)) {
                            Log.d("Query substring", Objects.requireNonNull(busInfo.get(arr))
                                    + "\nBus: " + arr[0]);
                            list.add(new String[]{arr[0], busInfo.get(arr)});
                            pres = true;
                        }
                    }
                    if (!pres) Log.d("Substring Error", "No substring found");
                    break;
                case MIXED:
                    //TODO
                    break;
                case UNKNOWN:
                    Log.e("UNKNOWN TYPE ERROR", "Cannot process that data...");
                    this.finish();
                    break;
                default:
                    //throw an exception
                    break;
            }
            if (list.size() < 1) return null;
            return list;
        }
        else {
            Log.e("Error", "An error occurred retrieving query");
            return null;
        }

    }
    private QueryType getType(String str){
        //process str first
        str = Parser.toParsable(str);
        //we could also make like a dictionary that stores common mistakes
        //for typical typos when searching?
        boolean isDigitOnly = true;
        boolean isAlphaOnly = true;
        for (int i = 0; i < str.length(); i++){
            if (!Character.isDigit(str.charAt(i))) isDigitOnly = false;
            else if (!Character.isAlphabetic(str.charAt(i))) isAlphaOnly = false;
        }
        return (isDigitOnly && isAlphaOnly) ? QueryType.UNKNOWN:
                        (isDigitOnly) ? QueryType.NUM_ONLY:
                                (isAlphaOnly) ? QueryType.ALPHA_ONLY:
                                        QueryType.MIXED;
    }

    private enum QueryType{
        NUM_ONLY,ALPHA_ONLY,MIXED,UNKNOWN
    }
}
