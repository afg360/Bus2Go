package dev.mainhq.bus2go.presentation.main.home

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.Tag

class TagsListElemAdapter(
	private var tags: List<Tag>,
	//private var selectedTag: Tag?,
	private var selectedTag: String?,
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
		holder.button.apply {
			text = tags[position].label
			setOnClickListener(onTagClick)
			if (text == selectedTag){
				setBackgroundColor(tags[position].color)
				setTextColor(holder.itemView.resources.getColor(R.color.dark, null))
				strokeColor = ColorStateList.valueOf(holder.itemView.resources.getColor(R.color.dark, null))
			}
			else {
				setBackgroundColor(holder.itemView.resources.getColor(R.color.dark, null))
				setTextColor(tags[position].color)
				strokeColor = ColorStateList.valueOf(tags[position].color)
			}
		}

	}

	override fun getItemCount(): Int {
		return tags.size
	}

	//TODO could use better functions, but for now that works
	fun updateTags(newTags: List<Tag>) {
		tags = newTags
		notifyDataSetChanged()
	}

	//fun updateSelectedTag(newTag: Tag?){
	fun updateSelectedTag(newTag: String?){
		val indexOldSelected = tags.map { it.label }.indexOf(selectedTag)
		selectedTag = newTag
		if (indexOldSelected >= 0) {
			notifyItemChanged(indexOldSelected)
		}
		tags.map { it.label }.indexOf(selectedTag).also {
			if (it >= 0){
				notifyItemChanged(it)
			}
		}
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