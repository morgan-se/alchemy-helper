package com.morgan.alchemyhelper

import java.io.InvalidObjectException

object IngredientRegistry {
    var registry = hashMapOf<String, Ingredient>()

    fun addToRegistry(ingredient: Ingredient) {
        registry[ingredient.getName()] = ingredient
    }

    fun addAllToRegistry(ingredients: List<Ingredient>) {
        ingredients.forEach { i -> registry[i.getName()] = i}
    }

    fun getFromRegistry(name: String): Ingredient {
        return registry[name] ?: throw InvalidObjectException("$name Not Found")
    }

    fun getAll(): List<Ingredient> {
        return registry.values.toList()
    }

}