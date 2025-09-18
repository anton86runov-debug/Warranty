package com.warranty.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WarrantyItemEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(WarrantyConverters::class)
abstract class WarrantyDatabase : RoomDatabase() {
    abstract fun warrantyDao(): WarrantyDao

    companion object {
        const val DATABASE_NAME = "warranty-db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE warranties ADD COLUMN reminder_enabled INTEGER NOT NULL DEFAULT 1"
                )
            }
        }
    }
}
