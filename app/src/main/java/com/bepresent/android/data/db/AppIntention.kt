package com.bepresent.android.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_intentions")
data class AppIntention(
    @PrimaryKey
    val id: String,
    val packageName: String,
    val appName: String,
    val allowedOpensPerDay: Int,
    val timePerOpenMinutes: Int,
    val totalOpensToday: Int = 0,
    val streak: Int = 0,
    val lastResetDate: String = "",
    val currentlyOpen: Boolean = false,
    val openedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
