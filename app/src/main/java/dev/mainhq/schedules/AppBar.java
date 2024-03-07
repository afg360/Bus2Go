package dev.mainhq.schedules;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

public class AppBar extends Fragment implements SearchView.OnQueryTextListener {

    public void onClickSettings(View view) {
            Intent intent = new Intent(this.getActivity(), Settings.class);
            startActivity(intent);
    }

    public interface OnConditionListener{
        void onConditionMet();
        void onConditionNotMet();
    }
    private OnConditionListener mListener;

    public AppBar() {
        super(R.layout.fragment_app_bar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_bar, container, false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query != null){
            Log.d("Query:", query);
            Intent intent = new Intent(this.getActivity(), Search.class);
            intent.putExtra("query", query);
            startActivity(intent);
            //searchView.clearFocus();
            return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
    private void checkCondition() {
        if (conditionIsMet()) {
            mListener.onConditionMet();
        }
        else {
            mListener.onConditionNotMet();
        }
    }

    private boolean conditionIsMet() {
        // Your condition logic here
        return true; // Example condition, replace with your actual condition
    }

    public void setOnConditionListener(OnConditionListener listener) {
        this.mListener = listener;
    }
}