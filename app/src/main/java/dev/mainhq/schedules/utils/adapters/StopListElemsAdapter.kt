package dev.mainhq.schedules.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R

class StopListElemsAdapter(private val data: List<String>)
    : RecyclerView.Adapter<StopListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.stop_list_elem, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = this.data[position]
        holder.stopNameTextView.text = data
    }

    override fun getItemCount(): Int { return this.data.size }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stopNameTextView: TextView

        init {
            stopNameTextView = view.findViewById(R.id.stop)
        }
    }
}