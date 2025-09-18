package com.warranty.app.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class WarrantyConverters {
    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()
}
