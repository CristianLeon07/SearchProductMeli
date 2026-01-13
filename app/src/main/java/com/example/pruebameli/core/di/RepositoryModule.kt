package com.example.pruebameli.core.di


import com.example.pruebameli.core.storage.TokenStorageImpl
import com.example.pruebameli.data.auth.AuthRepositoryImpl
import com.example.pruebameli.data.repository.ProductsRepositoryImpl
import com.example.pruebameli.domain.repository.AuthRepository
import com.example.pruebameli.domain.repository.ProductsRepository
import com.example.pruebameli.domain.repository.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductsRepository(
        impl: ProductsRepositoryImpl
    ): ProductsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTokenStorage(
        impl: TokenStorageImpl
    ): TokenStorage
}
