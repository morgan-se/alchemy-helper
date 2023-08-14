package com.morgan.alchemyhelper

import com.morgan.alchemyhelper.persistence.AppDatabase
import com.morgan.alchemyhelper.persistence.Effect
import com.morgan.alchemyhelper.persistence.Ingredient
import com.morgan.alchemyhelper.persistence.IngredientAndEffect
import java.io.InputStream
import android.util.Log

class CSVReader {
    fun readCSV(inputStream: InputStream, appDatabase: AppDatabase) {
        // Holy this code is bad, works tho :thinking:
        Log.d("CSV", "parsing csv....")
        val reader = inputStream.bufferedReader()
        val header = reader.readLine()
        reader.lineSequence().filter { it.isNotBlank() }
            .map {
                var (effect, desc, ingredients) = it.split(",", ignoreCase = false, limit = 3)
                if (effect.isNotBlank()) {
                    ingredients = ingredients.replace("\"", "")
                    val ingredient_list = ingredients.split(",")
                    var e = Effect(effect, desc)
                    var effectInDB: Effect = appDatabase.EffectDao().findByName(effect)
                    var effectInsertId: Long
                    if (effectInDB == null) {
                        effectInsertId = appDatabase.EffectDao().insert(e)
                    } else {
                        effectInsertId = effectInDB.effectId!!
                    }
                    for (i in ingredient_list) {
                        val ingredientName = i.trim()
                        if (ingredientName.isNotBlank()) {
                            var i2 = Ingredient(ingredientName, "")
                            var ingredientInDB: Ingredient =
                                appDatabase.IngredientDao().findByName(ingredientName)
                            var ingredientInsertId: Long
                            if (ingredientInDB == null) {
                                ingredientInsertId = appDatabase.IngredientDao().insert(i2)
                            } else {
                                ingredientInsertId = ingredientInDB.ingredientId!!
                            }
                            appDatabase.IngredientWithEffectsDao()
                                .insert(IngredientAndEffect(ingredientInsertId, effectInsertId))
                        }
                    }
                }
            }.toList()
    }

    fun main(args: Array<String>) {

    }
}