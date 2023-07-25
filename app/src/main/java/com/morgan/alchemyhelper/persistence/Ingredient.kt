package com.morgan.alchemyhelper.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices =[Index(value = ["name"], unique = true)])
class Ingredient(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String) {
    @PrimaryKey(autoGenerate = true) var ingredientId: Long? = null

    override fun toString(): String {
        return name
    }
}