package dev.mainhq.schedules;

import dev.mainhq.schedules.utils.BusListElemsAdapter;
import dev.mainhq.schedules.utils.RecyclerViewItemListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;

//instead of creating a new intent, just redo the list if search done in this activity
public class SearchBus extends AppCompatActivity {
    //processed query
    private Serializable queryResult;
    private Serializable dataType;
    //we tmp removed busInfo hashtable gotten from main
    //bcz doesnt work yet so duplicate code...
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.search_bus);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            //this.query = extras.getString("query");
            this.queryResult = extras.getSerializable("STMBusData");
            assert this.queryResult != null;
            if (! (queryResult instanceof ArrayList)) throw new IllegalStateException();
            //Object test = extras.get("STMBusData");
            /*
            String routesFile = "stm_data_info/routes.txt";
            ArrayList<String[]> dataQueried = Parser.setup(this, Parser.DataType.busList, this.query);
            //TODO if null, display "Sorry, no matches made"*/
            if (this.queryResult == null) {
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
                        ConstraintLayout layout = (ConstraintLayout) view;
                        String busName = ((TextView) layout.getChildAt(0)).getText().toString();
                        String busNum = ((TextView) layout.getChildAt(1)).getText().toString();
                        intent.putExtra("busName", busName);
                        intent.putExtra("busNum", busNum);
                        startActivity(intent);
                    }
                    @Override
                    public void onLongClick(View view, int position) {
                    }
                }));
                //need to improve that code to make it more safe
                recyclerView.setAdapter(new BusListElemsAdapter((ArrayList<String[]>) this.queryResult));
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
