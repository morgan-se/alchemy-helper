package com.morgan.alchemyhelper.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredient")
    fun getAll() : List<Ingredient>

    @Query("SELECT * FROM ingredient WHERE ingredientId = :id")
    fun findById(id: Int): Ingredient

    @Query("SELECT * FROM ingredient WHERE name LIKE :name")
    fun findByName(name: String): Ingredient

    @Insert
    fun insert(ingredient: Ingredient): Long

    @Insert
    fun insertAll(vararg ingredients: Ingredient)
}