package com.morgan.alchemyhelper.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface IngredientWithEffectsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ingredientWithEffects: IngredientAndEffect)

//    @Transaction
    @Query("SELECT * FROM ingredient")
    fun getIngredientWithEffects(): List<IngredientWithEffects>

    @Query("SELECT * FROM ingredient WHERE name=:name")
    fun findByIngredientName(name:String): IngredientWithEffects

    @Delete
    fun delete(ingredientWithEffects: IngredientAndEffect)
}