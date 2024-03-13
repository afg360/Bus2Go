package dev.mainhq.schedules;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
//TODO
//could add view/ontouchlistener to handle touch holding, etc.
//may need to use a recycler view, but implement a base adapter instead...?
public class BusListElemsAdapter extends RecyclerView.Adapter<BusListElemsAdapter.ViewHolder> {
    @NonNull
    private final ArrayList<String[]> busData;

    //when doing bus num >= 400, then color = green
    // if  >= 300, then color = black
    // else blue
    // if 700, then green (but same as 400)

    private interface Listener{
        void onClickListener();
    }
    BusListElemsAdapter(@NonNull ArrayList<String[]> data){
        super();
        this.busData = data;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bus_list_elem, parent, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] data = this.busData.get(position);
        holder.getBusNumView().setText(data[0]);
        holder.getBusDirView().setText(data[1]);
    }
    @Override
    public int getItemCount() {
        return this.busData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView busDir;
        private final TextView busNum;
        public ViewHolder(View view) {
            super(view);
            this.busDir = view.findViewById(R.id.busDir);
            this.busNum = view.findViewById(R.id.busNum);
        }
        public TextView getBusDirView() {
            return this.busDir;
        }
        public TextView getBusNumView() {
            return this.busNum;
        }
    }
}
