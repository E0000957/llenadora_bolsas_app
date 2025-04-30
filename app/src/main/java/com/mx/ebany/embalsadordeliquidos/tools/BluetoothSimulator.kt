package com.mx.ebany.embalsadordeliquidos.tools

import kotlin.math.pow

class BluetoothSimulator {
    private var receivedText: String = ""
    private var bagQuanty: Int = 0
    private var bagSize: Float = 0f
    private var liquidQuanty: Float = 0f
    private var timeCompress: Float = 0f
    private var timeCut: Float = 0f

    private val pi = 3.1415026
    private val diametroRodillo = 50.0
    private val constanteDeLlenado = 250.0

    fun receiveData4(data: String, callback: (String) -> Unit) {
        receivedText = data
        println("Recibido: $receivedText")

        if (receivedText.startsWith("START")) {
            processReceivedData(receivedText)
            val alturaBolsa = calculateHeight((bagSize.toDouble() * 2), liquidQuanty)
            val vueltasRodillo = calcularVueltas(diametroRodillo, alturaBolsa * 1000)

            for (quanty in 1..bagQuanty) {
                println("-- INICIA PROCESO DE LLENADO DE BOLSAS --")
                sendProcessToApp(quanty, "AA_", "_" + bagQuanty, callback)//Envio del estatus del proceso a app
                println("--SE ACTIVA EL MOTOR--")
                setRoller(vueltasRodillo)
                println("--SE APAGA EL MOTOR--")
                sendProcessToApp(quanty, "BB_", "_" + bagQuanty, callback)//Envio del estatus del proceso a app

                println("--SE ENCIENDEN LAS BOMBAS--")
                setBombsLiquid(constanteDeLlenado * liquidQuanty)
                println("--SE APAGAN LAS BOMBAS--")
                sendProcessToApp(quanty, "CC_", "_" + bagQuanty, callback)//Envio del estatus del proceso a app

                println("--SE SELLA LA BOLSA--")
                delay((timeCompress * 1000).toLong())
                sendProcessToApp(quanty, "DD_", "_" + bagQuanty, callback)//Envio del estatus del proceso a app

                println("--SE CORTAN LAS BOLSAS--")
                delay((timeCut * 1000).toLong())

                println("----------TERMINA UN PROCESO--------")
                if (endProcess()) break
            }
            sendProcessToApp(0, "EE_", "_" + bagQuanty, callback)//Envio del estatus del proceso a app
            println("-- PROCESO FINALIZADO --")
        }
    }

    private fun processReceivedData(data: String) {
        val tokens = data.removePrefix("START-").split("-")
        if (tokens.size >= 5) {
            bagQuanty = tokens[0].toInt()
            bagSize = tokens[1].toFloat()
            liquidQuanty = tokens[2].toFloat()
            timeCompress = tokens[3].toFloat()
            timeCut = tokens[4].toFloat()
        }
    }

    private fun calculateHeight(width: Double, volume: Float): Double {
        return volume / (pi * (width / 2).pow(2))
    }

    private fun calcularVueltas(diametro: Double, altura: Double): Double {
        return altura / (pi * diametro)
    }

    private fun setRoller(vueltas: Double) {
        println("Rodillo girando: $vueltas vueltas")
        delay(1000)
    }

    private fun setBombsLiquid(vueltas: Double) {
        println("Bomba activada por $vueltas pasos")
        delay(1000)
    }

    private fun endProcess(): Boolean {
        return receivedText.startsWith("STOP")
    }

    private fun delay(ms: Long) {
        Thread.sleep(ms)
    }

    private fun sendProcessToApp(
        quanty: Int,
        dataString: String,
        suffix: String,
        callback: (String) -> Unit
    ) {
        val data = dataString+quanty+suffix
        callback(data)
    }
}
