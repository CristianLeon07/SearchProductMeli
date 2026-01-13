package com.example.pruebameli.core.di

import com.example.pruebameli.data.repository.NetworkMonitorImpl
import com.example.pruebameli.domain.network.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para dependencias de la aplicación.
 * 
 * Este módulo proporciona bindings entre interfaces del dominio
 * y sus implementaciones concretas en la capa de datos.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Proporciona la implementación de NetworkMonitor.
     * 
     * @Binds permite a Dagger saber que NetworkMonitorImpl
     * es la implementación concreta de la interfaz NetworkMonitor.
     * 
     * Esto permite:
     * - Inyectar NetworkMonitor en ViewModels sin conocer la implementación
     * - Fácil testing: mockear NetworkMonitor en tests
     * - Desacoplamiento: cambiar implementación sin afectar consumidores
     * 
     * @param impl Implementación concreta (inyectada por Dagger)
     * @return La interfaz NetworkMonitor
     */
    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: NetworkMonitorImpl
    ): NetworkMonitor
}
