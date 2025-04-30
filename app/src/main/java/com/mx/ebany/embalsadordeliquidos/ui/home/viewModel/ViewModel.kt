package com.mx.ebany.embalsadordeliquidos.ui.home.viewModel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mx.ebany.embalsadordeliquidos.core.room.DaoConfiguration
import com.mx.ebany.embalsadordeliquidos.core.room.DataConfiguration
import kotlinx.coroutines.launch

class ViewModel(private val dao: DaoConfiguration) : ViewModel() {


    private var _getConfiguration: MutableLiveData<DataConfiguration> = MutableLiveData()
    var getConfiguration: LiveData<DataConfiguration> = _getConfiguration

    var _dataSave: MutableLiveData<DataConfiguration> = MutableLiveData()
    var dataSave: LiveData<DataConfiguration> = _dataSave

    fun addConfiguration(data: DataConfiguration, isConfig: Boolean = false) {
        viewModelScope.launch {
            val entities = dao.getAll()
            if(entities.isNullOrEmpty()){
                viewModelScope.launch {
                    dao.insert(data)
                    _dataSave.postValue(data)
                }
            }else{
                val dataTemp = entities.first()
                if (isConfig){
                    dataTemp.delayMotor = data.delayMotor
                    dataTemp.constanteLlenado = data.constanteLlenado
                }else{
                    dataTemp.cantidadBolsas = data.cantidadBolsas
                    dataTemp.anchoBolsa = data.anchoBolsa
                    dataTemp.cantidadLitros = data.cantidadLitros
                    dataTemp.tiempoCorte = data.tiempoCorte
                    dataTemp.tiempoSellado = data.tiempoSellado
                }

                updateConfiguration(dataTemp)
            }
        }

    }

    fun getDataConfiguration() {
        viewModelScope.launch {
            val entities = dao.getAll()
            if(entities.isNotEmpty()) _getConfiguration.postValue(entities.first())
        }
    }

    fun updateConfiguration(entity: DataConfiguration) {
        viewModelScope.launch {
            dao.update(entity)
            _dataSave.postValue(entity)
        }
    }
}