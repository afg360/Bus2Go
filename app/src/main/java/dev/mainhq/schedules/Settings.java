package dev.mainhq.schedules;

import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView backButton = new ImageView(this.getApplicationContext());
        backButton.setImageResource(R.drawable.baseline_arrow_back_24);
        this.setContentView(R.layout.settings);
        this.showLanguageMenu();
    }

    private void showLanguageMenu(){
        View lang_view = this.findViewById(R.id.language_view);
        lang_view.setOnClickListener(view -> {
            PopupMenu langMenu = new PopupMenu(getApplicationContext(), view);
            langMenu.getMenuInflater().inflate(R.menu.language_menu, langMenu.getMenu());
            langMenu.show();
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item != null) {
            item.setChecked(true);
            return true;
        }
        return false;
    }


    public void onClickLanguage(View view){

    }

    public void onClickTheme(View view){

    }
}
