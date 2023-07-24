package com.morgan.alchemyhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.morgan.alchemyhelper.persistence.Ingredient
import com.morgan.alchemyhelper.ui.theme.AlchemyHelperTheme

class MainActivity : ComponentActivity() {
    lateinit var db:RoomDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "alchemy-database").allowMainThreadQueries().build()
        // todo: Should really be replaced by Room (ORM) and file reading
        (db as AppDatabase).IngredientDao().insertAll(Ingredient("Tea Leaves", "Leaves of tea..."))

        setContent {
            AlchemyHelperTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "ItemListScreen") {
                    composable("ItemListScreen") {
                        ListWithHeader(ingredients = (db as AppDatabase).IngredientDao().getAll(), navController)
                    }
                    composable("ItemDetailScreen/{selectedItem}",
                        arguments = listOf(navArgument("selectedItem") {type= NavType.StringType})){
                        backStackEntry ->
                        val selectedItem = backStackEntry.arguments?.getString("selectedItem")
                        selectedItem?.let { string: String -> ItemDetailScreen(string)}
                    }
                }
            }
        }
    }
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ListWithHeader(ingredients: List<Ingredient>, navController: NavController) {
        LazyColumn (modifier = Modifier.fillMaxWidth(),
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

    @Composable
    fun IngredientRow(ingredient: Ingredient, navController: NavController) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("ItemDetailScreen/$ingredient")},
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = ingredient.toString())
        }
    }

    @Composable
    fun ItemDetailScreen(ingredientString: String) {
        val ingredient: Ingredient = (db as AppDatabase).IngredientDao().findByName(ingredientString)
        Text(text = "$ingredient Screen Here")
    }
}

