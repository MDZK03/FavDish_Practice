package com.example.favdish.view.fragments

import android.os.Bundle
import android.view.View
import com.example.favdish.base.BaseFragment
import com.example.favdish.databinding.FragmentRandomBinding

class RandomDishFragment : BaseFragment<FragmentRandomBinding>(
    FragmentRandomBinding::inflate
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}