package com.example.favdish.model.database

import androidx.annotation.WorkerThread
import com.example.favdish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow

class FavDishRepository(private val favDishDao: FavDishDao) {

    val allDishesList: Flow<List<FavDish>> = favDishDao.getDishesList()
    val favDishesList: Flow<List<FavDish>> = favDishDao.getFavoriteList()

    @WorkerThread
    suspend fun insertDishData(dish: FavDish) {
        favDishDao.insertDishDetails(dish)
    }

    @WorkerThread
    suspend fun updateDishData(dish: FavDish) {
        favDishDao.updateDishDetails(dish)
    }

    @WorkerThread
    suspend fun deleteDish(dish: FavDish) {
        favDishDao.deleteDish(dish)
    }

    @WorkerThread
    fun getFilteredList(filterName: String): Flow<List<FavDish>> = favDishDao.getFilteredList(filterName)

}