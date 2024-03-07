package dev.mainhq.schedules.menus;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import dev.mainhq.schedules.R;

public class Language extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.language);
        //this.showLanguageMenu();
    }
    /*
    private void showLanguageMenu(){
        View lang_view = this.findViewById(R.id.language_view);
        lang_view.setOnClickListener(view -> {
            PopupMenu langMenu = new PopupMenu(getApplicationContext(), view);
            langMenu.getMenuInflater().inflate(R.menu.language_menu, langMenu.getMenu());
            langMenu.show();
        });
    }
    /*
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item != null) {
            item.setChecked(true);
            return true;
        }
        return false;
    } */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        return true;
    }
}
