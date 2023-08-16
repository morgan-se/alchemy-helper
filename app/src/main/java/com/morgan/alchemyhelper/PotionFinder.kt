package com.morgan.alchemyhelper

import com.morgan.alchemyhelper.persistence.AppDatabase
import com.morgan.alchemyhelper.persistence.EffectWithIngredients
import com.morgan.alchemyhelper.persistence.Ingredient

class PotionFinder {
    fun findPossiblePotions(db: AppDatabase, ingredientIds: String): List<Potion> {
        // Kinda a mid implementation ngl
        val effects: List<EffectWithIngredients> = db.IngredientWithEffectsDao().getEffectWithIngredients()
        val ingredients: MutableList<Ingredient> = mutableListOf()
        val potions: MutableList<Potion> = mutableListOf()
        for (id in ingredientIds.split(",")) {
            val ingredient = db.IngredientDao().findById(id.toLong())
            if (ingredient != null)
                ingredients.add(ingredient)
        }
        for (e in effects) {
            val temp: MutableList<Ingredient> = mutableListOf()
            for (i in ingredients) {
               for (i2 in e.ingredient) {
                   if (i.sameAs(i2))
                       temp.add(i)
               }
            }
            if (temp.size >= 2) {
                // found a possible potion
                potions.add(Potion(e.effect, temp))
            }
        }
        return potions
    }
}