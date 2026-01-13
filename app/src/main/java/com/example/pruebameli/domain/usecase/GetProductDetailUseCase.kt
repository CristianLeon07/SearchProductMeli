package com.example.pruebameli.domain.usecase

import com.example.pruebameli.domain.common.ResourceData
import com.example.pruebameli.domain.models.ProductDetail
import com.example.pruebameli.domain.repository.ProductsRepository
import javax.inject.Inject

/**
 * Caso de uso que obtiene el detalle completo de un producto.
 *
 * Este UseCase:
 * - Valida que el ID del producto sea válido
 * - Valida el formato del ID (MCO seguido de números)
 */
class GetProductDetailUseCase @Inject constructor(
    private val repo: ProductsRepository
) {
    /**
     * Obtiene el detalle de un producto.
     *
     * @param id ID del producto (formato: MCO123456789)
     * @return ResourceData con el ProductDetail o un error
     */
    suspend operator fun invoke(id: String): ResourceData<ProductDetail> {
        // Validación: ID no puede estar vacío
        if (id.isBlank()) {
            return ResourceData.Error("El ID del producto no puede estar vacío")
        }


        // Los IDs de MercadoLibre generalmente tienen formato: MCO123456789
        val idPattern = Regex("^[A-Z]{3}[0-9]+$")
        if (!id.matches(idPattern)) {
            return ResourceData.Error("Formato de ID inválido. Debe ser similar a: MCO123456789")
        }

        return repo.getProductDetail(id)
    }
}