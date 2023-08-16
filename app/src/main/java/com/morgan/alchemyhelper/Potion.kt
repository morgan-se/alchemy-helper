package com.morgan.alchemyhelper

import com.morgan.alchemyhelper.persistence.Effect
import com.morgan.alchemyhelper.persistence.Ingredient

data class Potion(val effect: Effect, val ingredients: List<Ingredient>)