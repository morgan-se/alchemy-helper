package com.morgan.alchemyhelper.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Ingredient::class, Effect::class, IngredientAndEffect::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun IngredientDao(): IngredientDao
    abstract fun EffectDao(): EffectDao
    abstract fun IngredientWithEffectsDao(): IngredientWithEffectsDao
}