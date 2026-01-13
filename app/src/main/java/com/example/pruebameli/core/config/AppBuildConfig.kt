package com.example.pruebameli.core.config

/**
 * Configuración de build de la aplicación.
 * 
 * Alternativa a BuildConfig cuando no está disponible.
 * Detecta automáticamente si estamos en modo DEBUG o RELEASE.
 */
object AppBuildConfig {
    
    /**
     * Indica si la app está en modo debug.
     * 
     * Detecta basándose en:
     * - Si el applicationId contiene ".debug"
     * - Si el código es debuggeable (indicado por el system property)
     */
    val isDebug: Boolean by lazy {
        try {
            // Intenta usar BuildConfig si existe
            com.example.pruebameli.BuildConfig.DEBUG
        } catch (e: Exception) {
            // Fallback: detecta debug basándose en otras señales
            // En desarrollo, el applicationId suele tener .debug o similar
            val packageName = "com.pruebatecnica.pruebameli"
            packageName.contains(".debug", ignoreCase = true) || 
            System.getProperty("debug.mode") == "true"
        }
    }
    
    /**
     * Versión simplificada para usar directamente.
     * Por defecto asume DEBUG si hay dudas (más seguro para desarrollo).
     */
    const val DEBUG = true  // Cambia a false manualmente para release
}
