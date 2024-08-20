package com.example.favdish.view.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.favdish.App
import com.example.favdish.base.BaseFragment
import com.example.favdish.databinding.FragmentFavoriteBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.view.activities.MainActivity
import com.example.favdish.view.adapters.DishAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory

class FavoriteFragment : BaseFragment<FragmentFavoriteBinding>(
    FragmentFavoriteBinding::inflate
) {
    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as App).repository)
    }
    private lateinit var dishAdapter: DishAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvFavList.apply {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            dishAdapter = DishAdapter(this@FavoriteFragment)
            adapter = dishAdapter
        }
        favDishViewModel.favDishesList.observe(viewLifecycleOwner) {
            dishes ->
            dishes.let {
                if (it.isEmpty()) {
                    binding.rvFavList.visibility = View.GONE
                    binding.tvNoFavDish.visibility = View.VISIBLE
                } else {
                    binding.rvFavList.visibility = View.VISIBLE
                    binding.tvNoFavDish.visibility = View.GONE
                    dishAdapter.dishesList(it)
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.showBottomNavigationView()
        }
    }

    fun getDishDetails(favDish: FavDish) {
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.hideBottomNavigationView()
        }
        findNavController().navigate(FavoriteFragmentDirections.actionFavoriteToDishDetails(favDish))
    }
}