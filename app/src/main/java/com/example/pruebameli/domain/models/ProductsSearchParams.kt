package com.example.pruebameli.domain.models

import com.example.pruebameli.domain.config.AppConfig

/**
 * Parámetros para búsqueda de productos.
 *
 * Los valores por defecto se obtienen de la configuración centralizada (AppConfig)
 * para evitar hardcodear valores en múltiples lugares.
 *
 * @property query Término de búsqueda (requerido)
 * @property siteId ID del sitio de MercadoLibre (ej: MCO para Colombia)
 * @property domainId ID del dominio/categoría específica (opcional)
 * @property status Estado de los productos (ej: "active")
 * @property limit Cantidad de resultados por página
 * @property offset Desplazamiento para paginación
 */
data class ProductSearchParams(
    val query: String,
    val siteId: String = AppConfig.Search.DEFAULT_SITE_ID,
    val domainId: String? = null,
    val status: String? = null,
    val limit: Int = AppConfig.Search.PAGE_SIZE,
    val offset: Int = 0
)

