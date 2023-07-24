package com.morgan.alchemyhelper.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Ingredient::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun IngredientDao(): IngredientDao
}