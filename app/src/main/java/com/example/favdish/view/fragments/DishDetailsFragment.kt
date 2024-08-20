package com.example.favdish.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.safe.args.generator.ext.capitalize
import com.bumptech.glide.Glide
import com.example.favdish.App
import com.example.favdish.R
import com.example.favdish.databinding.FragmentDishDetailsBinding
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.util.Locale

class DishDetailsFragment : Fragment() {

    private var _binding: FragmentDishDetailsBinding? = null
    private val binding get() = _binding!!

    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as App).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: DishDetailsFragmentArgs by navArgs()

        args.let {
            try {
                Glide.with(requireActivity())
                    .load(args.dishDetails.image)
                    .into(binding.ivDishImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding.tvTitle.text = it.dishDetails.title
            binding.tvType.text = it.dishDetails.type.capitalize(Locale.ROOT)
            binding.tvCategory.text = it.dishDetails.category
            binding.tvIngredients.text = it.dishDetails.ingredients
            binding.tvCookingDirection.text = it.dishDetails.directionToCook
            binding.tvCookingTime.text = resources.getString(R.string.lbl_estimate_time, it.dishDetails.cookingTime)

            if (args.dishDetails.favoriteDish) {
                binding.ivFavorite.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite_selected, null))
            } else {
                binding.ivFavorite.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite_unselected, null))
            }
        }

        binding.ivFavorite.setOnClickListener {
            args.dishDetails.favoriteDish = !args.dishDetails.favoriteDish
            favDishViewModel.update(args.dishDetails)

            if (args.dishDetails.favoriteDish) {
                binding.ivFavorite.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite_selected, null))
                Snackbar.make(binding.root,getString(R.string.msg_add_favorite), 1000).show()
            } else {
                binding.ivFavorite.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite_unselected, null))
                Snackbar.make(binding.root,getString(R.string.msg_remove_favorite), 1000).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}