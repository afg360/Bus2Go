package dev.mainhq.schedules;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Search extends AppCompatActivity {
    String query;

    public Search(){
        super();
    }

    public Search(String query){
        super();
        this.query = query;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
