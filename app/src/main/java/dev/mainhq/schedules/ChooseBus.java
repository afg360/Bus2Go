package dev.mainhq.schedules;

import android.os.Bundle;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;

//todo
//change appbar to be only a back button
public class ChooseBus extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        assert extras != null;
        String busName = extras.getString("busName");
        String busNum = extras.getString("busNum");
        this.setContentView(R.layout.choose_bus);
        TextView busNumView = findViewById(R.id.chooseBusNum);
        busNumView.setText(busNum);
        TextView busNameView = findViewById(R.id.chooseBusDir);
        busNameView.setText(busName);
    }

    private void onClickBack(){
        View view = findViewById(R.id.back_button);
        view.setOnClickListener(v -> {
            finish();
        });
    }
}
