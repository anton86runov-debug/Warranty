package com.warranty.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.warranty.app.data.local.WarrantyDatabase
import com.warranty.app.data.repository.WarrantyRepositoryImpl
import com.warranty.app.domain.repository.WarrantyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WarrantyDatabase =
        Room.databaseBuilder(context, WarrantyDatabase::class.java, WarrantyDatabase.DATABASE_NAME)
            .addMigrations(WarrantyDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideWarrantyDao(database: WarrantyDatabase) = database.warrantyDao()

    @Provides
    @Singleton
    fun provideRepository(impl: WarrantyRepositoryImpl): WarrantyRepository = impl

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
