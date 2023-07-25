package com.morgan.alchemyhelper.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Effect(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String) {
    @PrimaryKey(autoGenerate = true) var effectId: Long? = null

    override fun toString(): String {
        return name
    }

}