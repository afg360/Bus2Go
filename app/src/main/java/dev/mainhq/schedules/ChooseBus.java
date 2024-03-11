package dev.mainhq.schedules;

import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//todo
//change appbar to be only a back button
public class ChooseBus extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TextView txtview = findViewById(R.id.back_abl_text);
        //txtview.setText(R.string.choose_a_direction);
        this.setContentView(R.layout.choose_bus);
        this.onClickBack();
    }

    private void onClickBack(){
        View view = findViewById(R.id.back_button);
        view.setOnClickListener(v -> {
            finish();
        });
    }
}
