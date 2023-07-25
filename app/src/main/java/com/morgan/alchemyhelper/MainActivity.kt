package com.morgan.alchemyhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import androidx.room.RoomDatabase
import com.morgan.alchemyhelper.persistence.AppDatabase
import com.morgan.alchemyhelper.persistence.Effect
import com.morgan.alchemyhelper.persistence.EffectWithIngredients
import com.morgan.alchemyhelper.persistence.Ingredient
import com.morgan.alchemyhelper.persistence.IngredientAndEffect
import com.morgan.alchemyhelper.persistence.IngredientWithEffects
import com.morgan.alchemyhelper.ui.theme.AlchemyHelperTheme

class MainActivity : ComponentActivity() {
    private lateinit var db: RoomDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "alchemy-database")
            .allowMainThreadQueries().build()
        (db as AppDatabase).clearAllTables()

        // todo: Should be replaced by file reading or populated default db
        (db as AppDatabase).IngredientWithEffectsDao().insert(
            IngredientAndEffect(
                (db as AppDatabase).IngredientDao()
                    .insert(Ingredient("Tea Leaves", "Leaves of tea...")),
                (db as AppDatabase).EffectDao()
                    .insert(Effect("Paralysis", "Enemy movements restricted"))
            )
        )

        setContent {
            AlchemyHelperTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "Home") {
                    composable("Home") {
                        HomeScreen(navController = navController)
                    }
                    composable("IngredientListScreen") {
                        IngredientListWithHeader(navController)
                    }
                    composable(
                        "IngredientDetailScreen/{selectedIngredient}",
                        arguments = listOf(navArgument("selectedIngredient") {
                            type = NavType.StringType
                        })
                    ) { backStackEntry ->
                        val selectedIngredient =
                            backStackEntry.arguments?.getString("selectedIngredient")
                        selectedIngredient?.let { string: String -> ItemDetailScreen(string) }
                    }
                    composable("EffectListScreen") {
                        EffectListWithHeader(navController)
                    }
                    composable(
                        "EffectDetailScreen/{selectedEffect}",
                        arguments = listOf(navArgument("selectedEffect") {
                            type = NavType.StringType
                        })
                    ) { backStackEntry ->
                        val selectedEffect = backStackEntry.arguments?.getString("selectedEffect")
                        selectedEffect?.let { string: String -> EffectDetailScreen(string) }
                    }
                }
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavController) {
        Column() {
            // Some form of header (maybe a logo) would be nice here
            Button(onClick = { navController.navigate("IngredientListScreen") }) {
                Text(text = "List of Ingredients")
            }
            Button(onClick = { navController.navigate("EffectListScreen") }) {
                Text(text = "List of Effects")
            }
            // more buttons for other functionality?
            // or maybe incorporate some other form of navigation such as button at the bottom
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun IngredientListWithHeader(navController: NavController) {
        val ingredients: List<Ingredient> = (db as AppDatabase).IngredientDao().getAll()
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            stickyHeader {
                Text(text = "Ingredients")
            }
            itemsIndexed(ingredients) { index, item ->
                IngredientRow(item, navController)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun EffectListWithHeader(navController: NavController) {
        val effects: List<Effect> = (db as AppDatabase).EffectDao().getAll()

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            stickyHeader {
                Text(text = "Effects")
            }
            itemsIndexed(effects) { index, item ->
                EffectRow(item, navController)
            }
        }
    }

    @Composable
    fun IngredientRow(ingredient: Ingredient, navController: NavController) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { navController.navigate("IngredientDetailScreen/$ingredient") },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = ingredient.toString())
        }
    }

    @Composable
    fun EffectRow(effect: Effect, navController: NavController) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { navController.navigate("EffectDetailScreen/$effect") },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = effect.toString())
        }
    }

    @Composable
    fun ItemDetailScreen(ingredientString: String) {
        val ingredientWithEffects: IngredientWithEffects =
            (db as AppDatabase).IngredientWithEffectsDao().findByIngredientName(ingredientString)
        Column {
            Text(text = "${ingredientWithEffects.ingredient.name} Screen Here")
            Text(text = "${ingredientWithEffects.effect}")
        }
    }

    @Composable
    fun EffectDetailScreen(effectString: String) {
        val effectWithIngredients: EffectWithIngredients =
            (db as AppDatabase).IngredientWithEffectsDao().findByEffectName(effectString)
        Column {
            Text(text = "${effectWithIngredients.effect.name} Screen Here")
            Text(text = "${effectWithIngredients.ingredient}")
        }
    }
}

