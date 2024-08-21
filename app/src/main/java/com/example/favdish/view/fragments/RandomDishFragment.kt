package com.example.favdish.view.fragments

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.favdish.App
import com.example.favdish.R
import com.example.favdish.base.BaseFragment
import com.example.favdish.databinding.FragmentRandomDishBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.model.entities.RandomDish
import com.example.favdish.utils.Constants
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.example.favdish.viewmodel.RandomDishViewModel
import timber.log.Timber

class RandomDishFragment : BaseFragment<FragmentRandomDishBinding>(
    FragmentRandomDishBinding::inflate
) {
    private lateinit var randomViewModel: RandomDishViewModel
    private var progressDialog: Dialog? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        randomViewModel = ViewModelProvider(this)[RandomDishViewModel::class.java]

        randomViewModel.getRandomDishFromApi()

        randomDishObserver()

        binding.srlRandomDish.setOnClickListener {
            randomViewModel.getRandomDishFromApi()
        }
    }

    private fun randomDishObserver() {
        randomViewModel.randomDishResponse.observe(viewLifecycleOwner) { randomDishResponse ->
            randomDishResponse?.let {
                if (binding.srlRandomDish.isRefreshing) {
                    binding.srlRandomDish.isRefreshing = false
                }

                setRandomDishResponseInUI(randomDishResponse.recipes[0])
            }
        }

        randomViewModel.loadError.observe(viewLifecycleOwner) { dataError ->
            dataError?.let {
                Timber.tag("Random Dish API Error").i("$dataError")

                if (binding.srlRandomDish.isRefreshing) {
                    binding.srlRandomDish.isRefreshing = false
                }
            }
        }

        randomViewModel.loadRandomDish.observe(viewLifecycleOwner) { loadRandomDish ->
            loadRandomDish?.let {
                Timber.tag("Random Dish API Error").i("$loadRandomDish")

                if (loadRandomDish && !binding.srlRandomDish.isRefreshing) {
                    showCustomProgressDialog()
                } else {
                    hideProgressDialog()
                }
            }
        }
    }

    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {
        binding.ivDishImage.setImageDrawable(Drawable.createFromPath(recipe.image))
        binding.tvTitle.text = recipe.title

        var type = "Other"
        if (recipe.dishTypes.isNotEmpty()) {
            type = recipe.dishTypes[0]
            binding.tvType.text = type
        }

        binding.tvCategory.text = resources.getString(R.string.lbl_other)

        var ingredients = ""
        for (value in recipe.extendedIngredients) {
            ingredients = if (ingredients.isEmpty()) {
                value.original
            } else {
                ingredients + ", \n" + value.original
            }
        }
        binding.tvIngredients.text = ingredients

        binding.tvCookingTime.text =
            resources.getString(R.string.lbl_estimate_cooking_time, recipe.readyInMinutes.toString())

        binding.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_favorite_unselected))

        var addedToFavorite = false

        binding.ivFavoriteDish.setOnClickListener{
            if (addedToFavorite) {
                Toast.makeText(requireActivity(),
                    resources.getString(R.string.msg_already_added), Toast.LENGTH_SHORT).show()
            } else {
                val randomDishDetails = FavDish(
                    recipe.image, Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title, type, "Other", ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )

                val favDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as App).repository) }

                favDishViewModel.insert(randomDishDetails)

                addedToFavorite = true

                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_favorite_selected))

                Toast.makeText(requireActivity(),
                    resources.getString(R.string.msg_add_favorite), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showCustomProgressDialog() {
        progressDialog = Dialog(requireActivity())

        progressDialog?.let {
            it.setContentView(R.layout.dialog_custom_progress)
            it.show()
        }
    }

    private fun hideProgressDialog() { progressDialog?.dismiss() }
}