package com.warranty.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class WarrantyDaoTest {

    private lateinit var database: WarrantyDatabase
    private lateinit var dao: WarrantyDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WarrantyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.warrantyDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadWarranty() = runBlocking {
        val entity = WarrantyItemEntity(
            name = "Laptop",
            purchaseDate = LocalDate.of(2024, 1, 1),
            expirationDate = LocalDate.of(2025, 1, 1),
            reminderEnabled = true
        )

        val id = dao.insert(entity)
        val items = dao.observeAll().first()

        assertThat(id).isGreaterThan(0)
        assertThat(items).hasSize(1)
        assertThat(items.first().name).isEqualTo("Laptop")
    }

    @Test
    fun migrationFrom1To2_addsReminderColumn() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration-test"
        context.deleteDatabase(dbName)

        createVersion1Database(context, dbName)

        val migratedDb = Room.databaseBuilder(context, WarrantyDatabase::class.java, dbName)
            .addMigrations(WarrantyDatabase.MIGRATION_1_2)
            .build()

        migratedDb.openHelper.writableDatabase.apply {
            query("PRAGMA table_info('warranties')").use { cursor ->
                var hasColumn = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    if (columnName == "reminder_enabled") {
                        hasColumn = true
                        break
                    }
                }
                assertThat(hasColumn).isTrue()
            }
            query("SELECT reminder_enabled FROM warranties").use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getInt(0)).isEqualTo(1)
            }
            close()
        }
        migratedDb.close()
    }

    private fun createVersion1Database(context: Context, dbName: String) {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS warranties (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, category TEXT, price REAL, store TEXT, purchase_date INTEGER NOT NULL, expiration_date INTEGER, duration_months INTEGER)"
                    )
                    db.execSQL(
                        "INSERT INTO warranties (name, category, price, store, purchase_date, expiration_date, duration_months) VALUES ('Camera', 'Electronics', 899.0, 'TechStore', 20000, 21000, NULL)"
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        helper.writableDatabase.close()
        helper.close()
    }
}
