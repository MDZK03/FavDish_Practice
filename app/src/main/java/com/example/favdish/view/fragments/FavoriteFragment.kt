package com.example.favdish.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.favdish.App
import com.example.favdish.databinding.FragmentFavoriteBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.view.activities.MainActivity
import com.example.favdish.view.adapters.DishAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as App).repository)
    }
    private lateinit var dishAdapter: DishAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getDishDetails(favDish: FavDish) {
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.hideBottomNavigationView()
        }
        findNavController().navigate(FavoriteFragmentDirections.actionFavoriteToDishDetails(favDish))
    }
}