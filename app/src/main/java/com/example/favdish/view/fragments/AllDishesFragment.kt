package com.example.favdish.view.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.favdish.App
import com.example.favdish.R
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.databinding.FragmentAllDishesBinding
import com.example.favdish.utils.Constants
import com.example.favdish.view.activities.AddDishActivity
import com.example.favdish.view.adapters.DishAdapter
import com.example.favdish.view.adapters.ListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.example.favdish.viewmodel.HomeViewModel
import timber.log.Timber

class AllDishesFragment : Fragment() {
    private lateinit var binding: FragmentAllDishesBinding
    private lateinit var customDialog: Dialog
    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as App).repository)
    }
    private lateinit var dishAdapter: DishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.all_dishes_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add_dish -> {
                        startActivity(Intent(requireActivity(),AddDishActivity::class.java))
                        true
                    }
                    R.id.action_filter_dishes -> {
                        filterDishesListDialog()
                        true
                    }
                    else -> false
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
            binding = FragmentAllDishesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvDishesList.apply {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            dishAdapter = DishAdapter(this@AllDishesFragment)
            adapter = dishAdapter
        }

        favDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
            dishes.let {
                if (it.isNotEmpty()) {
                    binding.rvDishesList.visibility = View.VISIBLE
                    binding.tvNoDish.visibility = View.GONE
                    dishAdapter.dishesList(it)

                } else {
                    binding.rvDishesList.visibility = View.GONE
                    binding.tvNoDish.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun filterDishesListDialog() {
        customDialog = Dialog(requireActivity())
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        customDialog.setContentView(binding.root)

        binding.tvDialogTitle.text = resources.getString(R.string.title_select_item)

        val dishTypes = Constants.dishTypes()
        dishTypes.add(0, Constants.ALL_ITEMS)

        binding.rvDialogList.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = ListItemAdapter(
            requireActivity(),
            this@AllDishesFragment,
            dishTypes,
            Constants.FILTER_SELECTION
        )
        binding.rvDialogList.adapter = adapter
        customDialog.show()
    }


    fun filterSelection(filterItemSelection: String) {
        customDialog.dismiss()
        Timber.tag("Filter Selection").d(filterItemSelection)

        if (filterItemSelection == Constants.ALL_ITEMS) {
            favDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes ->
                dishes.let {
                    if (it.isNotEmpty()) {
                        binding.rvDishesList.visibility = View.VISIBLE
                        binding.tvNoDish.visibility = View.GONE
                        dishAdapter.dishesList(it)

                    } else {
                        binding.rvDishesList.visibility = View.GONE
                        binding.tvNoDish.visibility = View.VISIBLE
                    }
                }
            }
        } else {
        }
    }
}