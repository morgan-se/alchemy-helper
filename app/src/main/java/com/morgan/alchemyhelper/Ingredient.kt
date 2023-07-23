package com.morgan.alchemyhelper

class Ingredient(private val name: String) {
    fun getName(): String {
        return name;
    }

    override fun toString(): String {
        return name;
    }
}