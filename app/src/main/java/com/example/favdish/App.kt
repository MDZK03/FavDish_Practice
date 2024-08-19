package com.example.favdish

import android.app.Application
import com.example.favdish.model.database.FavDishRepository
import com.example.favdish.model.database.FavDishRoomDatabase


class App : Application() {
    private val database by lazy { FavDishRoomDatabase.getDatabase(this@App) }

    val repository by lazy { FavDishRepository(database.favDishDao()) }
}