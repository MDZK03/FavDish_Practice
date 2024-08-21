package com.example.favdish.view.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.favdish.databinding.ItemCustomListBinding
import com.example.favdish.view.activities.AddEditDishActivity
import com.example.favdish.view.fragments.AllDishesFragment

class ListItemAdapter(
    private val activity: Activity,
    private val fragment: Fragment?,
    private val listItems: List<String>,
    private val selection: String
) : RecyclerView.Adapter<ListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCustomListBinding =
            ItemCustomListBinding.inflate(LayoutInflater.from(activity), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listItems[position]

        holder.tvText.text = item

        holder.itemView.setOnClickListener {

            if (activity is AddEditDishActivity) {
                activity.selectedListItem(item, selection)
            }

            if (fragment is AllDishesFragment) {
                fragment.filterSelection(item)
            }
        }
    }

    override fun getItemCount(): Int { return listItems.size }

    class ViewHolder(binding: ItemCustomListBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvText = binding.tvItemText
    }
}