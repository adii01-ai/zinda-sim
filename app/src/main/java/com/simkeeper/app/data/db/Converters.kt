package com.simkeeper.app.data.db

import androidx.room.TypeConverter
import com.simkeeper.app.domain.model.Carrier
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun carrierToString(carrier: Carrier): String = carrier.name

    @TypeConverter
    fun stringToCarrier(value: String): Carrier =
        Carrier.entries.firstOrNull { it.name == value } ?: Carrier.OTHER
}
