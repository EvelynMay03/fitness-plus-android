package com.example.register

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.register.data.ActivityEntity

class ActivityTypeAdapter(
    private val onItemClick: (ActivityEntity) -> Unit,
) : RecyclerView.Adapter<ActivityTypeAdapter.ViewHolder>() {

    private val items = mutableListOf<ActivityEntity>()
    private val iconBackgrounds = listOf(
        R.drawable.home_category_icon_creme,
        R.drawable.home_category_icon_mustard,
        R.drawable.home_category_icon_navy_soft,
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtActivityEmoji: TextView = view.findViewById(R.id.txtActivityEmoji)
        val txtActivityName: TextView = view.findViewById(R.id.txtActivityName)
        val txtActivityDesc: TextView = view.findViewById(R.id.txtActivityDesc)
    }

    fun submitList(activities: List<ActivityEntity>) {
        items.clear()
        items.addAll(activities)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_type, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = items[position]
        holder.txtActivityEmoji.apply {
            text = activity.emoji.orEmpty()
            setBackgroundResource(iconBackgrounds[position % iconBackgrounds.size])
        }
        holder.txtActivityName.text = activity.name
        holder.txtActivityDesc.text = activity.description.orEmpty()
        holder.itemView.setOnClickListener { onItemClick(activity) }
    }

    override fun getItemCount(): Int = items.size
}
