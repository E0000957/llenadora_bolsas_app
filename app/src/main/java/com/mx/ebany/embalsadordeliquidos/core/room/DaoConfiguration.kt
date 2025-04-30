package com.mx.ebany.embalsadordeliquidos.core.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface DaoConfiguration {
    @Insert
    suspend fun insert(dataConfiguration: DataConfiguration)

    @Query("SELECT * FROM datos_configuracion")
    suspend fun getAll(): List<DataConfiguration>

    @Update
    suspend fun update(dataConfiguration: DataConfiguration)

}