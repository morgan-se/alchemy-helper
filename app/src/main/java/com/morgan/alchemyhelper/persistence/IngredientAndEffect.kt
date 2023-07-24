package com.morgan.alchemyhelper.persistence

import androidx.room.Entity


@Entity(primaryKeys=["ingredientId","effectId"])
data class IngredientAndEffect(val ingredientId: Int, val effectId: Int)