package com.morgan.alchemyhelper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.morgan.LevenshteinDistance
import com.morgan.alchemyhelper.persistence.AppDatabase
import com.morgan.alchemyhelper.persistence.Effect
import com.morgan.alchemyhelper.persistence.EffectWithIngredients
import com.morgan.alchemyhelper.persistence.Ingredient
import com.morgan.alchemyhelper.persistence.IngredientWithEffects
import com.morgan.alchemyhelper.ui.theme.AlchemyHelperTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects


class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MAIN", "Starting app...")
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "alchemy-database")
            .allowMainThreadQueries().build()
        db.clearAllTables()
        val csvId = R.raw.skyrim_alchemy
        Log.d("MAIN", "Loading csv with id $csvId")

        CSVReader().readCSV(applicationContext.resources.openRawResource(csvId), db)

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
                    composable("FindPotions") {
                        IngredientSelection(navController = navController)
                    }
                    composable(
                        "PossiblePotions/{ingredients}",
                        arguments = listOf(navArgument("ingredients") {
                            type = NavType.StringType
                        })
                    ) { backStackEntry ->
                        val ingredientIds = backStackEntry.arguments?.getString("ingredients")
                        ingredientIds?.let { string: String -> PossiblePotions(ingredientIds = string) }
                    }
                    composable("GetImage") {
                        GetImage(navController)
                    }
                }
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavController) {
        val svgId = R.drawable.chem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 16.dp, 0.dp, 0.dp)
        ) {
            Image(painterResource(id = svgId), "simple logo")
            Text(text = "Alchemy Helper", textAlign = TextAlign.Center, fontSize = 28.sp)
            Row(modifier = Modifier.padding(0.dp, 10.dp)) {
                Button(onClick = { navController.navigate("IngredientListScreen") }) {
                    Text(text = "List of Ingredients")
                }
                Button(onClick = { navController.navigate("EffectListScreen") }) {
                    Text(text = "List of Effects")
                }
            }
            Divider(thickness = 1.dp, modifier = Modifier.padding(2.dp, 0.dp, 2.dp, 10.dp))
            Button(onClick = { navController.navigate("FindPotions") }) {
                Text(text = "Find possible potions from selection")
            }
            Button(onClick = { navController.navigate("GetImage") }) {
                Text(text = "Find possible potions from image")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "© Morgan English")
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun IngredientListWithHeader(navController: NavController) {
        val ingredients: List<Ingredient> = db.IngredientDao().getAll()
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            stickyHeader {
                Surface(modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)) {
                    Text(text = "Ingredients", fontSize = 20.sp)
                }
            }
            itemsIndexed(ingredients) { index, item ->
                IngredientRow(item, navController)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun EffectListWithHeader(navController: NavController) {
        val effects: List<Effect> = db.EffectDao().getAll()

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            stickyHeader() {
                Surface(modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)) {
                    Text(text = "Effects", fontSize = 20.sp)
                }
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
                .padding(8.dp)
                .clickable { navController.navigate("IngredientDetailScreen/$ingredient") },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = ingredient.toString())
            // todo: onclick open uesp page with ingredient (should just be added to url)
        }
    }

    @Composable
    fun EffectRow(effect: Effect, navController: NavController) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.navigate("EffectDetailScreen/$effect") },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = effect.toString())
        }
    }

    @Composable
    fun ItemDetailScreen(ingredientString: String) {
        val ingredientWithEffects: IngredientWithEffects =
            db.IngredientWithEffectsDao().findByIngredientName(ingredientString)
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ingredientWithEffects.ingredient.name,
                fontSize = 20.sp,
                fontStyle = FontStyle.Italic
            )
            Text(text = "Effects:")
            ingredientWithEffects.effect.forEach {
                Text(text = "- ${it.name}")
            }
            Button(onClick = {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://en.uesp.net/wiki/Skyrim:${
                            ingredientWithEffects.ingredient.name.replace(
                                " ",
                                "_"
                            )
                        }"
                    )
                )
                startActivity(browserIntent)
            }) {
                Text(text = "Open in UESP")
            }
        }
    }

    @Composable
    fun EffectDetailScreen(effectString: String) {
        val effectWithIngredients: EffectWithIngredients =
            db.IngredientWithEffectsDao().findByEffectName(effectString)
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = effectWithIngredients.effect.name,
                fontSize = 20.sp,
                fontStyle = FontStyle.Italic
            )
            Text(text = "Ingredients:")
            effectWithIngredients.ingredient.forEach {
                Text(text = "- ${it.name}")
            }
        }
    }

    @Composable
    fun SelectableIngredientRow(
        ingredient: Ingredient,
        selectedIngredients: MutableList<Ingredient>
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selectedIngredients.contains(ingredient), onCheckedChange = {
                Log.d("INGREDIENTS", ingredient.toString())
                if (selectedIngredients.contains(ingredient)) {
                    selectedIngredients.remove(ingredient)
                } else {
                    Log.d("INGREDIENTS", "Adding ingredient...")
                    selectedIngredients.add(ingredient)
                }
            })
            Text(text = ingredient.toString())
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun IngredientSelection(navController: NavController) {
        val ingredients: List<Ingredient> = db.IngredientDao().getAll()
        val selectedIngredients = remember { mutableStateListOf<Ingredient>() }
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                stickyHeader {
                    Surface(modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)) {
                        Text(text = "Ingredients", fontSize = 20.sp)
                    }
                }
                // possible todo: add search field
                itemsIndexed(ingredients) { index, item ->
                    SelectableIngredientRow(item, selectedIngredients = selectedIngredients)
                }
            }
            Button(
                onClick = {
                    if (selectedIngredients.isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Make sure to select some ingredients first!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        navController.navigate(
                            "PossiblePotions/${
                                selectedIngredients.map { it.ingredientId }.joinToString(",")
                            }"
                        )
                    }
                },
                modifier = Modifier
                    .padding(0.dp, 0.dp, 8.dp, 6.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Text(text = "Find potions")
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun PossiblePotions(ingredientIds: String) {
        Log.d("POTIONS", ingredientIds)
        val potions: List<Potion> = PotionFinder().findPossiblePotions(db, ingredientIds)
        val selectedPotion = remember { mutableStateOf<Potion?>(null) }
        if (potions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Whoops looks like you cant create a potion with what you've got there...",
                    fontSize = 20.sp, modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                stickyHeader {
                    Surface(modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)) {
                        Text(text = "Ingredients", fontSize = 20.sp)
                    }
                }
                itemsIndexed(potions) { index, item ->
                    PotionRow(item, selectedPotion = selectedPotion)
                }
            }
        }
    }

    @Composable
    fun PotionRow(potion: Potion, selectedPotion: MutableState<Potion?>) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    if (selectedPotion.value == potion) {
                        selectedPotion.value = null
                    } else {
                        selectedPotion.value = potion
                    }
                },

            ) {
            Text(text = potion.effect.name, fontSize = 18.sp, fontStyle = FontStyle.Italic)
            Text(
                text = potion.effect.description,
                modifier = Modifier.padding(8.dp, 2.dp, 0.dp, 0.dp)
            )
            if (selectedPotion.value == potion) {
                Text(
                    text = "Possible Ingredients: ",
                    modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp)
                )
                potion.ingredients.forEach {
                    Text(text = it.name, modifier = Modifier.padding(4.dp, 2.dp, 0.dp, 0.dp))
                }
            }
        }
    }

    /**
     * Adapted from
     * https://medium.com/@dheerubhadoria/capturing-images-from-camera-in-android-with-jetpack-compose-a-step-by-step-guide-64cd7f52e5de
     * and
     * https://developers.google.com/ml-kit/vision/text-recognition/v2/android
     */
    @OptIn(ExperimentalCoilApi::class)
    @Composable
    fun GetImage(navController: NavController) {
        val context = applicationContext
        val imageUris = remember { mutableStateListOf<Uri>() }
        val file = context.createImageFile()
        val uri = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            "$packageName.provider", file
        )
        val cameraLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
                imageUris.add(uri)
            }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (imageUris.isEmpty()) {
                Text(text = "No images yet", fontSize = 20.sp, fontStyle = FontStyle.Italic)
            } else {
                LazyVerticalGrid(modifier = Modifier.fillMaxSize(), columns = GridCells.Fixed(count = 2),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    itemsIndexed(imageUris) { index, item ->
                        if (item.path?.isNotEmpty() == true) {
                            // this doesn't work without setting a specific height, not gonna ask why
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f).height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    painter = rememberImagePainter(item),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                if (imageUris.isNotEmpty()) {
                    Button(onClick = {
                        try {
                            Toast.makeText(
                                context,
                                "Processing images and calculating possible potions. May take a few seconds...",
                                Toast.LENGTH_SHORT
                            ).show()
                            val ingredients: MutableList<Ingredient> = mutableListOf()
                            for (imageUri in imageUris) {
                                val recognizer =
                                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                val image: InputImage = InputImage.fromFilePath(context, imageUri)
                                recognizer.process(image).addOnSuccessListener { text ->
                                    val distanceCalc = LevenshteinDistance()
                                    for (block in text.textBlocks) {
                                        for (line in block.lines) {
                                            val l =
                                                line.text.replace("[^A-Za-z0-9 ']", "")
                                                    .replace(")", "")
                                            for (ingredient in db.IngredientDao().getAll()) {
                                                // value below checks for some level of equality with what is found
                                                // getting a good number is more of an art than science
                                                if (distanceCalc.findSimilarity(
                                                        l,
                                                        ingredient.name
                                                    ) > 0.7
                                                ) {
                                                    ingredients.add(ingredient)
                                                }
                                            }
                                        }
                                    }
                                    if (ingredients.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Couldn't find any ingredients, give it another go",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        navController.navigate(
                                            "PossiblePotions/${
                                                ingredients.distinctBy{it.name}.map { it.ingredientId }
                                                    .joinToString(",")
                                            }"
                                        )
                                    }
                                    Log.d("OCR", text.text)
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Whoops something went wrong, give it another go",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }) {
                        Text(text = "This looks good")
                    }
                }
                Button(onClick = {
                    val permissionCheckResult =
                        ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        )
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(uri)
                    } else {
                        // Request permissions
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }) {
                    if(imageUris.isNotEmpty()) {
                        Text(text = "Capture Another Image From Camera")
                    } else {
                        Text(text = "Capture An Image From Camera")

                    }
                }
            }
        }


    }


    private fun Context.createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            externalCacheDir      /* directory */
        )
        return image
    }
}

