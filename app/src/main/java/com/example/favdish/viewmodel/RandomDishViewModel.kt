package com.example.favdish.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.favdish.model.entities.RandomDish
import com.example.favdish.model.network.RandomDishApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class RandomDishViewModel : ViewModel() {
    private val disposable = CompositeDisposable()

    private val apiService = RandomDishApiService()

    val loadRandomDish = MutableLiveData<Boolean>()
    val randomDishResponse = MutableLiveData<RandomDish.Recipes>()
    val loadError = MutableLiveData<Boolean>()

    fun getRandomDishFromApi() {
        loadRandomDish.value = true

        val subscription = apiService.getRandomDish()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object: DisposableSingleObserver<RandomDish.Recipes>() {
                override fun onSuccess(t: RandomDish.Recipes) {
                    loadRandomDish.value = false
                    randomDishResponse.value = t
                    loadError.value = false
                }

                override fun onError(e: Throwable) {
                    loadRandomDish.value = false
                    loadError.value = true
                    e.printStackTrace()
                }
            })

        disposable.add(subscription)
    }
}