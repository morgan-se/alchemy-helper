package com.morgan.alchemyhelper

import com.morgan.alchemyhelper.persistence.AppDatabase
import com.morgan.alchemyhelper.persistence.Effect
import com.morgan.alchemyhelper.persistence.Ingredient
import com.morgan.alchemyhelper.persistence.IngredientAndEffect
import java.io.InputStream
import android.util.Log

class CSVReader {
    fun readCSV(inputStream: InputStream, appDatabase: AppDatabase) {
        Log.d("CSV", "parsing csv....")
        val reader = inputStream.bufferedReader()
        reader.lineSequence().filter { it.isNotBlank() }
            .map {
                val (effect, desc, ingredients) = it.split(",", ignoreCase = false, limit = 3)
                val effectInDB: Effect? = appDatabase.EffectDao().findByName(effect)
                val effectInsertId: Long = if (effectInDB == null) {
                    appDatabase.EffectDao().insert(Effect(effect, desc))
                } else {
                    effectInDB.effectId!!
                }
                for (ingredient in ingredients.replace("\"", "").split(",")) {
                    if (ingredient.isBlank())
                        return@map
                    val ingredientName = ingredient.trim()
                    val ingredientInDB: Ingredient? =
                        appDatabase.IngredientDao().findByName(ingredientName)
                    val ingredientInsertId: Long = if (ingredientInDB == null) {
                        appDatabase.IngredientDao().insert(Ingredient(ingredientName, ""))
                    } else {
                        ingredientInDB.ingredientId!!
                    }
                    appDatabase.IngredientWithEffectsDao()
                        .insert(IngredientAndEffect(ingredientInsertId, effectInsertId))
                }
            }.toList()
    }

    fun main(args: Array<String>) {

    }
}