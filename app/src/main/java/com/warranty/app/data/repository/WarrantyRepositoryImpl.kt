package com.warranty.app.data.repository

import com.warranty.app.data.local.WarrantyDao
import com.warranty.app.data.local.WarrantyItemEntity
import com.warranty.app.domain.model.WarrantyItem
import com.warranty.app.domain.repository.WarrantyRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WarrantyRepositoryImpl @Inject constructor(
    private val dao: WarrantyDao
) : WarrantyRepository {

    override fun observe(): Flow<List<WarrantyItem>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun findById(id: Long): WarrantyItem? =
        dao.findById(id)?.toDomain()

    override suspend fun upsert(item: WarrantyItem): Long {
        val entity = item.toEntity()
        return dao.insert(entity)
    }

    override suspend fun upsert(items: List<WarrantyItem>) {
        dao.insert(items.map { it.toEntity() })
    }

    override suspend fun delete(itemId: Long) {
        dao.findById(itemId)?.let { dao.delete(it) }
    }

    override suspend fun clear() {
        dao.clear()
    }
}

private fun WarrantyItemEntity.toDomain(): WarrantyItem =
    WarrantyItem(
        id = id,
        name = name,
        category = category,
        price = price,
        store = store,
        purchaseDate = purchaseDate,
        expirationDate = expirationDate,
        durationMonths = durationMonths,
        reminderEnabled = reminderEnabled
    )

private fun WarrantyItem.toEntity(): WarrantyItemEntity =
    WarrantyItemEntity(
        id = id,
        name = name,
        category = category,
        price = price,
        store = store,
        purchaseDate = purchaseDate,
        expirationDate = expirationDate,
        durationMonths = durationMonths,
        reminderEnabled = reminderEnabled
    )
