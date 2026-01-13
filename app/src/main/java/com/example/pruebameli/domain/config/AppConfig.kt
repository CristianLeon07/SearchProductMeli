package com.example.pruebameli.domain.config

/**
 * Configuración centralizada de la aplicación.
 * Contiene constantes y valores por defecto usados en la lógica de negocio.
 */
object AppConfig {
    
    /**
     * Configuración de búsqueda de productos
     */
    object Search {
        /** ID del sitio por defecto (Colombia) */
        const val DEFAULT_SITE_ID = "MCO"
        
        /** Estado de productos a buscar */
        const val DEFAULT_STATUS = "active"
        
        /** Cantidad de items por página */
        const val PAGE_SIZE = 20
        
        /** Tamaño mínimo de página permitido */
        const val MIN_PAGE_SIZE = 10
        
        /** Tamaño máximo de página permitido */
        const val MAX_PAGE_SIZE = 50
    }
    
    /**
     * Configuración de autenticación
     */
    object Auth {
        /** Ventana de tiempo antes de refrescar el token (en minutos) */
        const val TOKEN_REFRESH_WINDOW_MINUTES = 25
        
        /** Ventana de tiempo en segundos */
        const val TOKEN_REFRESH_WINDOW_SECONDS = TOKEN_REFRESH_WINDOW_MINUTES * 60L
    }
    
    /**
     * Configuración de Flow y coroutines
     */
    object Flow {
        /** 
         * Timeout en milisegundos para StateFlow.stateIn() con WhileSubscribed.
         * Mantiene el Flow activo 5 segundos después de que el último suscriptor se desconecta.
         * Esto evita recrear el Flow si el usuario vuelve rápidamente (ej: cambio de pestaña).
         */
        const val STATE_FLOW_TIMEOUT_MS = 5_000L
    }
    
    /**
     * Configuración de UI y eventos
     */
    object UI {
        /**
         * Buffer extra para SharedFlow de eventos one-shot.
         * Permite que un evento se emita incluso si no hay colectores activos en ese momento.
         */
        const val EVENT_BUFFER_CAPACITY = 1
    }
}
