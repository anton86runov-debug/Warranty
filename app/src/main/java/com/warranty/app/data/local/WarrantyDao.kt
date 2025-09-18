package com.warranty.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WarrantyDao {
    @Query("SELECT * FROM warranties ORDER BY expiration_date IS NULL, expiration_date ASC")
    fun observeAll(): Flow<List<WarrantyItemEntity>>

    @Query("SELECT * FROM warranties WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): WarrantyItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WarrantyItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<WarrantyItemEntity>): List<Long>

    @Update
    suspend fun update(item: WarrantyItemEntity)

    @Delete
    suspend fun delete(item: WarrantyItemEntity)

    @Query("DELETE FROM warranties")
    suspend fun clear()
}
