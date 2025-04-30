package com.mx.ebany.embalsadordeliquidos.core.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datos_configuracion")
data class DataConfiguration (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "delayMotor") var delayMotor: String = "",
    @ColumnInfo(name = "constanteLlenado") var constanteLlenado: String = "",
    @ColumnInfo(name = "cantidadBolsas") var cantidadBolsas: String = "",
    @ColumnInfo(name = "anchoBolsa") var anchoBolsa: String = "",
    @ColumnInfo(name = "cantidadLitros") var cantidadLitros: String = "",
    @ColumnInfo(name = "tiempoCorte") var tiempoCorte: String = "",
    @ColumnInfo(name = "tiempoSellado") var tiempoSellado: String = ""

)