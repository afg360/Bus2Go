package dev.mainhq.bus2go.presentation.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.Tag

class TagsListElemAdapter(
	private var tags: List<Tag>,
	private val onTagClick: (View) -> Unit
): RecyclerView.Adapter<TagsListElemAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			LayoutInflater.from(parent.context)
				.inflate(R.layout.elem_tags_list, parent, false)
		)
	}

	override fun onBindViewHolder(
		holder: ViewHolder,
		position: Int,
	) {
		holder.button.text = tags[position].label
		holder.button.setOnClickListener(onTagClick)
	}

	override fun getItemCount(): Int {
		return tags.size
	}

	//TODO could use better functions, but for now that works
	fun updateTags(newTags: List<Tag>) {
		tags = newTags
		notifyDataSetChanged()
	}

	/*
	fun addTag(tagtoAdd: String) {
		tags = tags.toMutableList().apply { add(tagtoAdd) }
		notifyItemInserted(tags.size - 1)
	}

	fun removeTag(tagToRemove: String){
		val index = tags.indexOf(tagToRemove)
		if (index < 0) throw IllegalStateException("Somethings wrong in your removing tag implementation")
		tags = tags.toMutableList().apply { remove(tagToRemove) }
		notifyItemRemoved(index)
	}
	 */

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val button: MaterialButton = itemView.findViewById(R.id.tag_elem_button)
	}

}