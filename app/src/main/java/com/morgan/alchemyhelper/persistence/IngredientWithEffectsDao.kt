package com.morgan.alchemyhelper.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
//import androidx.room.Transaction

@Dao
interface IngredientWithEffectsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ingredientWithEffects: IngredientAndEffect)

//    @Transaction
    @Query("SELECT * FROM ingredient")
    fun getIngredientWithEffects(): List<IngredientWithEffects>

    @Query("SELECT * FROM effect")
    fun getEffectWithIngredients(): List<EffectWithIngredients>

    @Query("SELECT * FROM ingredient WHERE name=:name")
    fun findByIngredientName(name:String): IngredientWithEffects

    @Query("SELECT * FROM effect WHERE name=:name")
    fun findByEffectName(name: String): EffectWithIngredients

    @Delete
    fun delete(ingredientWithEffects: IngredientAndEffect)
}