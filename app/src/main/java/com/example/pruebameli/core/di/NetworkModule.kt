package com.example.pruebameli.core.di


import com.example.pruebameli.core.config.AppBuildConfig
import com.example.pruebameli.core.config.MeliAuthConfig
import com.example.pruebameli.data.auth.OAuthApi
import com.example.pruebameli.data.network.interceptor.BearerInterceptor
import com.example.pruebameli.data.remote.SearchProductsApi
import com.example.pruebameli.data.repository.ProductsRepositoryImpl
import com.example.pruebameli.domain.repository.ProductsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton


/**
 * Módulo de Dagger Hilt que proporciona las dependencias de red de la aplicación.
 * 
 * Este módulo centraliza la configuración de la capa de red incluyendo:
 * - Clientes HTTP (OkHttpClient) con diferentes configuraciones según el tipo de endpoint
 * - Instancias de Retrofit para diferentes APIs
 * - Interceptores para logging y autenticación
 * - Conversores JSON con Moshi
 * - APIs específicas de Mercado Libre (OAuth y búsqueda de productos)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    // CONSTANTES PARA NAMED QUALIFIERS
    
    /** Qualifier para el OkHttpClient usado en flujos de autenticación OAuth */
    private const val AUTH_OKHTTP = "auth_okhttp"
    
    /** Qualifier para el Retrofit usado en la API de OAuth */
    private const val AUTH_RETROFIT = "auth_retrofit"

    /** Qualifier para el OkHttpClient usado en endpoints públicos sin autenticación */
    private const val PUBLIC_OKHTTP_REFRESH_TOKEN = "public_okhttp"
    
    /** Qualifier para el Retrofit usado en endpoints públicos */
    private const val PUBLIC_RETROFIT = "public_retrofit"

    /** Qualifier para el OkHttpClient usado en endpoints que requieren Bearer token */
    private const val PRIVATE_OKHTTP_REFRESH_TOKEN = "private_okhttp"
    
    /** Qualifier para el Retrofit usado en la API de búsqueda de productos autenticada */
    private const val PRIVATE_RETROFIT_SEARCH = "private_retrofit"


    // INTERCEPTORES Y CONVERSORES COMPARTIDOS

    /**
     * Proporciona el interceptor de logging HTTP para todas las peticiones de red.
     * 
     * Configuración:
     * - En modo DEBUG: registra el cuerpo completo de requests y responses para facilitar debugging
     * - En modo RELEASE: desactiva completamente los logs para proteger datos sensibles (tokens, datos de usuario)
     * 
     * @return HttpLoggingInterceptor configurado según el build type
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (AppBuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    /**
     * Proporciona el conversor JSON Moshi para serialización/deserialización de objetos Kotlin.
     * 
     * Incluye KotlinJsonAdapterFactory para soporte completo de:
     * - Data classes
     * - Valores por defecto en parámetros
     * - Nullability de Kotlin
     * 
     * @return Moshi configurado para trabajar con clases Kotlin
     */
    @Provides
    @Singleton
    fun provideMoshiJsonConverter(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()


    // CLIENTES HTTP (OkHttpClient)

    /**
     * Proporciona un OkHttpClient específico para peticiones de autenticación OAuth.
     * 
     * Características:
     * - NO incluye BearerInterceptor (las peticiones OAuth usan Basic Auth o client credentials)
     * - Incluye logging interceptor para debugging
     * - Usado exclusivamente para el flujo de intercambio de código por token
     * 
     * @param logging Interceptor de logging HTTP
     * @return OkHttpClient configurado para autenticación OAuth sin Bearer token
     */
    @Provides
    @Singleton
    @Named(AUTH_OKHTTP)
    fun provideOkHttpClientForOAuthAuthentication(
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

    /**
     * Proporciona un OkHttpClient para endpoints públicos de Mercado Libre.
     * 
     * Características:
     * - NO requiere autenticación (sin Bearer token)
     * - Incluye logging interceptor para debugging
     * - Usado para consultas que no requieren sesión de usuario
     * 
     * @param logging Interceptor de logging HTTP
     * @return OkHttpClient configurado para endpoints públicos
     */
    @Provides
    @Singleton
    @Named(PUBLIC_OKHTTP_REFRESH_TOKEN)
    fun provideOkHttpClientForPublicEndpoints(
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

    /**
     * Proporciona un OkHttpClient para endpoints autenticados de Mercado Libre.
     * 
     * Características:
     * - INCLUYE BearerInterceptor que agrega automáticamente el header "Authorization: Bearer {token}"
     * - Incluye logging interceptor para debugging
     * - Usado para todas las peticiones que requieren autenticación de usuario
     * - El BearerInterceptor se ejecuta ANTES del logging para que los logs muestren el header completo
     * 
     * @param bearerInterceptor Interceptor que inyecta el token de acceso en cada petición
     * @param logging Interceptor de logging HTTP
     * @return OkHttpClient configurado para endpoints autenticados con Bearer token
     */
    @Provides
    @Singleton
    @Named(PRIVATE_OKHTTP_REFRESH_TOKEN)
    fun provideOkHttpClientWithBearerToken(
        bearerInterceptor: BearerInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(bearerInterceptor)
            .addInterceptor(logging)
            .build()


    // INSTANCIAS DE RETROFIT

    /**
     * Proporciona una instancia de Retrofit configurada para la API de OAuth de Mercado Libre.
     * 
     * Configuración:
     * - Base URL: endpoint de autenticación de Mercado Libre
     * - Cliente HTTP: OkHttpClient sin Bearer token (autenticación OAuth usa otros mecanismos)
     * - Conversor: Moshi para JSON
     * 
     * Endpoints soportados:
     * - POST /oauth/token - Intercambio de authorization code por access token
     * 
     * @param okHttp Cliente HTTP específico para OAuth (sin Bearer interceptor)
     * @param moshi Conversor JSON
     * @return Retrofit configurado para la API de OAuth
     */
    @Provides
    @Singleton
    @Named(AUTH_RETROFIT)
    fun provideRetrofitForOAuthApi(
        @Named(AUTH_OKHTTP) okHttp: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(MeliAuthConfig.API_BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    /**
     * Proporciona una instancia de Retrofit configurada para endpoints públicos de Mercado Libre.
     * 
     * Configuración:
     * - Base URL: API base de Mercado Libre
     * - Cliente HTTP: OkHttpClient sin autenticación
     * - Conversor: Moshi para JSON
     * 
     * Uso: Endpoints que no requieren sesión de usuario activa
     * 
     * @param okHttp Cliente HTTP para endpoints públicos
     * @param moshi Conversor JSON
     * @return Retrofit configurado para endpoints públicos
     */
    @Provides
    @Singleton
    @Named(PUBLIC_RETROFIT)
    fun provideRetrofitForPublicEndpoints(
        @Named(PUBLIC_OKHTTP_REFRESH_TOKEN) okHttp: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(MeliAuthConfig.API_BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    /**
     * Proporciona una instancia de Retrofit configurada para la API autenticada de Mercado Libre.
     * 
     * Configuración:
     * - Base URL: API base de Mercado Libre
     * - Cliente HTTP: OkHttpClient CON BearerInterceptor (inyecta token automáticamente)
     * - Conversor: Moshi para JSON
     * 
     * Endpoints soportados:
     * - GET /sites/{site}/search - Búsqueda de productos con autenticación
     * - Todos los endpoints que requieran "Authorization: Bearer {token}"
     * 
     * @param okHttp Cliente HTTP con Bearer token interceptor
     * @param moshi Conversor JSON
     * @return Retrofit configurado para la API autenticada de búsqueda y productos
     */
    @Provides
    @Singleton
    @Named(PRIVATE_RETROFIT_SEARCH)
    fun provideRetrofitForAuthenticatedApi(
        @Named(PRIVATE_OKHTTP_REFRESH_TOKEN) okHttp: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(MeliAuthConfig.API_BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()


    // INTERFACES DE API DE MERCADO LIBRE

    /**
     * Proporciona la implementación de la interfaz OAuthApi para el flujo de autenticación.
     * 
     * Responsabilidades:
     * - Intercambiar el authorization code por un access token
     * - Renovar tokens expirados (refresh token flow)
     * 
     * Usa el Retrofit configurado para OAuth (sin Bearer token en las peticiones)
     * 
     * @param retrofit Instancia de Retrofit específica para OAuth
     * @return Implementación de OAuthApi generada por Retrofit
     */
    @Provides
    @Singleton
    fun provideMercadoLibreOAuthApi(
        @Named(AUTH_RETROFIT) retrofit: Retrofit
    ): OAuthApi = retrofit.create(OAuthApi::class.java)

    /**
     * Proporciona la implementación de la interfaz SearchProductsApi para búsqueda de productos.
     * 
     * Responsabilidades:
     * - Búsqueda de productos en Mercado Libre por query
     * - Filtrado y ordenamiento de resultados
     * - Paginación de resultados (offset, limit)
     * - Obtención de detalles de productos específicos
     * 
     * Usa el Retrofit configurado con autenticación (incluye Bearer token automáticamente)
     * 
     * @param retrofit Instancia de Retrofit para API autenticada
     * @return Implementación de SearchProductsApi generada por Retrofit
     */
    @Provides
    @Singleton
    fun provideMercadoLibreSearchProductsApi(
        @Named(PRIVATE_RETROFIT_SEARCH) retrofit: Retrofit
    ): SearchProductsApi = retrofit.create(SearchProductsApi::class.java)

}
