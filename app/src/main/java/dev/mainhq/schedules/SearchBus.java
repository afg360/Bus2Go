package dev.mainhq.schedules;

import dev.mainhq.schedules.utils.Parser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.String;
import java.util.ArrayList;
//instead of creating a new intent, just redo the list if search done in this activity
public class SearchBus extends AppCompatActivity {
    //processed query
    private String query;
    //we tmp removed busInfo hashtable gotten from main
    //bcz doesnt work yet so duplicate code...
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.search_bus);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.query = extras.getString("query");
            String routesFile = "stm_data_info/routes.txt";
            ArrayList<String[]> dataQueried = Parser.setup(this, Parser.DataType.busList, this.query);
            //if null, display "Sorry, no matches made"
            if (dataQueried == null) {
                String empty = "No Matches Found";
                TextView txtview = new TextView(this.getApplicationContext());
            }
            else{
                RecyclerView recyclerView = this.findViewById(R.id.search_recycle_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.addOnItemTouchListener(new RecyclerViewItemListener(getApplicationContext(), recyclerView, new RecyclerViewItemListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent intent = new Intent(getApplicationContext(), ChooseBus.class);
                        startActivity(intent);
                    }
                    @Override
                    public void onLongClick(View view, int position) {
                    }
                }));
                recyclerView.setAdapter(new BusListElemsAdapter(dataQueried));
            }
        }
        else Log.d("Query", "None");
        this.onClickBack();
    }

    private void chooseBus(View view){
        TextView bus = (TextView) view.findViewById(R.id.busDir);
        TextView busNum = (TextView) view.findViewById(R.id.busNum);
    }
    private void onClickBack(){
        View view = findViewById(R.id.back_button);
        if (view != null) {
            view.setOnClickListener(v -> {
                finish();
            });
            this.finish();
        }
    }
}
