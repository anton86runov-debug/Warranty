package com.warranty.app.domain.repository

import com.warranty.app.domain.model.WarrantyItem
import kotlinx.coroutines.flow.Flow

interface WarrantyRepository {
    fun observe(): Flow<List<WarrantyItem>>
    suspend fun findById(id: Long): WarrantyItem?
    suspend fun upsert(item: WarrantyItem): Long
    suspend fun upsert(items: List<WarrantyItem>)
    suspend fun delete(itemId: Long)
    suspend fun clear()
}
