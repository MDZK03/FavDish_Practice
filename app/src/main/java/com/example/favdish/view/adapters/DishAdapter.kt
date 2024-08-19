package com.example.favdish.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.favdish.databinding.ItemDishLayoutBinding
import com.example.favdish.model.entities.FavDish


class DishAdapter(private val fragment: Fragment) : RecyclerView.Adapter<DishAdapter.ViewHolder>() {
    private var dishes: List<FavDish> = listOf()
    class ViewHolder(binding: ItemDishLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivDishImage = binding.ivDishImage
        val tvDishTitle = binding.tvDishTitle
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishAdapter.ViewHolder {
        val binding = ItemDishLayoutBinding.inflate(LayoutInflater.from(fragment.requireContext()), parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishAdapter.ViewHolder, position: Int) {
        val dish = dishes[position]
        Glide.with(fragment)
            .load(dish.image)
            .into(holder.ivDishImage)
        holder.tvDishTitle.text = dish.title
    }

    override fun getItemCount(): Int {
        return dishes.size
    }

    fun dishesList(list: List<FavDish>) {
        dishes = list
        notifyDataSetChanged()
    }

}