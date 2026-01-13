package com.example.pruebameli.domain.usecase

import com.example.pruebameli.domain.config.AppConfig
import com.example.pruebameli.domain.repository.ProductsRepository
import javax.inject.Inject
import com.example.pruebameli.domain.models.ProductSearchParams


/**
 * Caso de uso que busca productos paginados según un query.
 *
 * Este UseCase:
 * - Valida y limpia el query de búsqueda
 * - Aplica configuración por defecto (sitio, estado, límite)
 * - Retorna un flujo de datos paginados (PagingData)
 *
 * Ejemplo de uso:
 * ```kotlin
 * val products = searchProductsPagedUseCase("celular")
 *     .cachedIn(viewModelScope)
 * ```
 */
class SearchProductsPagedUseCase @Inject constructor(
    private val repo: ProductsRepository
) {
    /**
     * Busca productos paginados.
     *
     * @param query Término de búsqueda (se limpiará automáticamente)
     * @return Flow de PagingData con los productos encontrados
     */
    operator fun invoke(query: String) =
        repo.searchProductsPaged(
            ProductSearchParams(
                query = query.trim(),
                
                // Configuración centralizada en Domain
                siteId = AppConfig.Search.DEFAULT_SITE_ID,
                status = AppConfig.Search.DEFAULT_STATUS,
                limit = AppConfig.Search.PAGE_SIZE,
                domainId = null,
                offset = 0
            )
        )
}

