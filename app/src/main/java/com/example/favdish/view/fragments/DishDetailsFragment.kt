package com.example.favdish.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.navigation.safe.args.generator.ext.capitalize
import com.bumptech.glide.Glide
import com.example.favdish.R
import com.example.favdish.databinding.FragmentDishDetailsBinding
import java.io.IOException
import java.util.Locale

class DishDetailsFragment : Fragment() {

    private var _binding: FragmentDishDetailsBinding? = null
    private val binding get() = _binding!!

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

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}