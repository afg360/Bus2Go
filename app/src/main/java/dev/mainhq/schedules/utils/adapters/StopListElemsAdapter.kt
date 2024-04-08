package dev.mainhq.schedules.utils.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import dev.mainhq.schedules.R
import dev.mainhq.schedules.Times

class StopListElemsAdapter(private val data: List<String>, private val headsign: String)
    : RecyclerView.Adapter<StopListElemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.stop_list_elem, parent, false),
            headsign
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = this.data[position]
        holder.stopNameTextView.text = data
        holder.onClick(holder.itemView)
    }

    override fun getItemCount(): Int {
        return this.data.size
    }

    class ViewHolder(view: View, private val headsign : String) : RecyclerView.ViewHolder(view), OnClickListener {
        val stopNameTextView: TextView
        init {
            stopNameTextView = view.findViewById(R.id.stop)
        }

        override fun onClick(view: View?) {
            view?.setOnClickListener {
                val textView: TextView =
                    (it as ConstraintLayout).getChildAt(0)!! as TextView
                val stopName = textView.text as String
                val intent = Intent(view.context, Times::class.java)
                intent.putExtra("stopName", stopName)
                intent.putExtra("headsign", headsign)
                it.context.startActivity(intent)
                it.clearFocus()
            }
        }
    }
}