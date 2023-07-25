package com.morgan.alchemyhelper.persistence

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class EffectWithIngredients (
    @Embedded var effect: Effect,
    @Relation(
        parentColumn = "effectId",
        entity = Ingredient::class,
        entityColumn = "ingredientId",
        associateBy = Junction(
            value = IngredientAndEffect::class,
            parentColumn = "effectId",
            entityColumn = "ingredientId"
        )
    )
    var ingredient: List<Ingredient>
)