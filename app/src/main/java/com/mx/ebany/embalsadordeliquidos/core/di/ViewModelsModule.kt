package com.mx.ebany.embalsadordeliquidos.core.di

import com.mx.ebany.embalsadordeliquidos.core.room.AppDataBase
import com.mx.ebany.embalsadordeliquidos.ui.home.viewModel.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val ViewModelsModule = module {

    // Proveer la base de datos y el DAO
    single { AppDataBase.getDatabase(get()).myEntityDao() }

    // Registrar el ViewModel
    viewModel { ViewModel(get()) }
}