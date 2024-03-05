package dev.mainhq.schedules;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BusListElemsAdapter extends RecyclerView.Adapter<BusListElemsAdapter.ViewHolder> {

    private ArrayList<String[]> busData;

    BusListElemsAdapter(ArrayList<String[]> data){
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
        //count how many items listed
        return this.busData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView busDir;
        private final TextView busNum;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            this.busDir = (TextView) view.findViewById(R.id.busDir);
            this.busNum = (TextView) view.findViewById(R.id.busNum);
        }
        public TextView getBusDirView() {
            return this.busDir;
        }
        public TextView getBusNumView() {
            return this.busNum;
        }
    }
}
