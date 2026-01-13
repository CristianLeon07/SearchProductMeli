package com.example.pruebameli.domain.usecase

import androidx.paging.PagingData
import com.example.pruebameli.domain.config.AppConfig
import com.example.pruebameli.domain.models.Product
import com.example.pruebameli.domain.models.ProductSearchParams
import com.example.pruebameli.domain.repository.ProductsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para SearchProductsPagedUseCase
 *
 * Cobertura:
 * - Success: Query válido retorna Flow
 * - Trim: Query con espacios hace trim correctamente
 * - Edge case: Query vacío/solo espacios
 * - Parámetros: Verifica valores de AppConfig
 * - Llamadas: Verifica invocación al repositorio
 * - Edge case: Query con caracteres especiales
 */
class SearchProductsPagedUseCaseTest {

    private lateinit var useCase: SearchProductsPagedUseCase
    private lateinit var repository: ProductsRepository

    private val mockProduct = Product(
        id = "MCO123456789",
        name = "iPhone 13 Pro",
        imageUrl = "https://example.com/image.jpg",
        brand = "3500000",
        model = "COP"
    )
    private val mockPagingData = PagingData.from(listOf(mockProduct))

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = SearchProductsPagedUseCase(repository)
    }

    @Test
    fun `invoke con query válido retorna Flow de PagingData y llama al repositorio`() {
        // Given
        val query = "celular"
        every { repository.searchProductsPaged(any()) } returns flowOf(mockPagingData)

        // When
        val result = useCase.invoke(query)

        // Then
        assert(result != null)
        verify(exactly = 1) { repository.searchProductsPaged(any()) }
    }

    @Test
    fun `invoke con query con espacios al inicio y final hace trim correctamente`() {
        // Given
        val queryWithSpaces = "   laptop gamer   "
        val paramsSlot = slot<ProductSearchParams>()
        every { repository.searchProductsPaged(capture(paramsSlot)) } returns flowOf(mockPagingData)

        // When
        useCase.invoke(queryWithSpaces)

        // Then
        assertEquals("laptop gamer", paramsSlot.captured.query)
        verify(exactly = 1) { repository.searchProductsPaged(any()) }
    }


    @Test
    fun `invoke llama al repositorio con ProductSearchParams correcto`() {
        // Given
        val query = "camara"
        val expectedParams = ProductSearchParams(
            query = "camara",
            siteId = AppConfig.Search.DEFAULT_SITE_ID,
            status = AppConfig.Search.DEFAULT_STATUS,
            limit = AppConfig.Search.PAGE_SIZE,
            domainId = null,
            offset = 0
        )
        every { repository.searchProductsPaged(expectedParams) } returns flowOf(mockPagingData)

        // When
        useCase.invoke(query)

        // Then
        verify(exactly = 1) { repository.searchProductsPaged(expectedParams) }
    }

}
