package com.morgan.alchemyhelper.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IngredientWithEffectsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ingredientWithEffects: IngredientAndEffect)

    @Query("SELECT * FROM ingredient")
    fun getIngredientWithEffects(): List<IngredientWithEffects>

    @Query("SELECT * FROM effect")
    fun getEffectWithIngredients(): List<EffectWithIngredients>

    @Query("SELECT * FROM ingredient WHERE name=:name")
    fun findByIngredientName(name:String): IngredientWithEffects

    @Query("SELECT * FROM effect WHERE name=:name")
    fun findByEffectName(name: String): EffectWithIngredients

    @Query("SELECT * FROM effect WHERE effectId=:id")
    fun findByEffectId(id: Long): EffectWithIngredients?

    @Delete
    fun delete(ingredientWithEffects: IngredientAndEffect)
}