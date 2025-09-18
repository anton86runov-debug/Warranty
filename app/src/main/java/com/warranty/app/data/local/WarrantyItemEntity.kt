package com.warranty.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "warranties")
data class WarrantyItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String? = null,
    val price: Double? = null,
    val store: String? = null,
    @ColumnInfo(name = "purchase_date")
    val purchaseDate: LocalDate,
    @ColumnInfo(name = "expiration_date")
    val expirationDate: LocalDate? = null,
    @ColumnInfo(name = "duration_months")
    val durationMonths: Int? = null,
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = true
)
