package com.morgan.alchemyhelper.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EffectDao {

    @Query("SELECT * From effect ORDER BY name ASC")
    fun getAll(): List<Effect>

    @Query("SELECT * FROM effect WHERE effectId=:id")
    fun findById(id: Int): Effect

    @Query("SELECT * FROM effect WHERE name=:name")
    fun findByName(name: String): Effect

    @Insert
    fun insert(effect: Effect): Long

    @Insert
    fun insertAll(vararg effects: Effect)
}