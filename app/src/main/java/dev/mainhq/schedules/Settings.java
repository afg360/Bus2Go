package dev.mainhq.schedules;

import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//TODO
//NEED TO CHANGE APPBAR
//NEED TO IMPROVE MENU
//NEED TO MAKE THEME CLICKABLE
public class Settings extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.settings);
        this.showLanguageMenu();
        this.onClickBack();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.back_button) {
            finish();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item != null) {
            item.setChecked(true);
            return true;
        }
        return false;
    }
    public void onClickTheme(View view){

    }
    private void onClickBack(){
        View view = findViewById(R.id.back_button);
        if (view != null) {
            view.setOnClickListener(v -> {
                finish();
            });
        }
    }
}
