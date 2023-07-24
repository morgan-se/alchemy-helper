package com.morgan.alchemyhelper.persistence

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class IngredientWithEffects(
    @Embedded var ingredient: Ingredient,
    @Relation(
        parentColumn = "ingredientId",
        entity = Effect::class,
        entityColumn = "effectId",
        associateBy = Junction(
            value = IngredientAndEffect::class,
            parentColumn = "ingredientId",
            entityColumn = "effectId"
        )
    )
    var effect: List<Effect>
) {
}