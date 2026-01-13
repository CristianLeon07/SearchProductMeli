package com.example.pruebameli.domain.usecase


import com.example.pruebameli.domain.common.ResourceData
import com.example.pruebameli.domain.models.ProductDetail
import com.example.pruebameli.domain.repository.ProductsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para GetProductDetailUseCase
 *
 * Cobertura:
 * Success cases - IDs válidos
 *  Error cases - Validaciones y errores de repositorio
 * Edge cases - Formatos límite y espacios
 */
class GetProductDetailUseCaseTest {

    private lateinit var useCase: GetProductDetailUseCase

    // Mock del repositorio
    private lateinit var repository: ProductsRepository

    // Datos de prueba
    private val validProductId = "MCO123456789"
    private val mockProductDetail = ProductDetail(
        id = validProductId,
        name = "iPhone 13 Pro Max",
        imageUrls = listOf("https://example.com/image1.jpg"),
        description = "Smartphone de alta gama"
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetProductDetailUseCase(repository)
    }

    // SUCCESS CASES


    @Test
    fun `invoke con ID válido MCO retorna Success con ProductDetail`() = runTest {
        // Given
        val productId = "MCO123456789"
        val expectedResult = ResourceData.Success(mockProductDetail)
        coEvery { repository.getProductDetail(productId) } returns expectedResult

        // When
        val result = useCase.invoke(productId)

        // Then
        assertTrue(result is ResourceData.Success)
        assertEquals(mockProductDetail, (result as ResourceData.Success).data)
        coVerify(exactly = 1) { repository.getProductDetail(productId) }
    }

    @Test
    fun `invoke con ID válido MLA retorna Success`() = runTest {
        // Given - IDs de Argentina
        val productId = "MLA987654321"
        val productDetail = mockProductDetail.copy(id = productId)
        coEvery { repository.getProductDetail(productId) } returns ResourceData.Success(
            productDetail
        )

        // When
        val result = useCase.invoke(productId)

        // Then
        assertTrue(result is ResourceData.Success)
        assertEquals(productId, (result as ResourceData.Success).data.id)
        coVerify(exactly = 1) { repository.getProductDetail(productId) }
    }

    @Test
    fun `invoke con ID válido MLB retorna Success`() = runTest {
        // Given - IDs de Brasil
        val productId = "MLB555666777"
        val productDetail = mockProductDetail.copy(id = productId)
        coEvery { repository.getProductDetail(productId) } returns ResourceData.Success(
            productDetail
        )

        // When
        val result = useCase.invoke(productId)

        // Then
        assertTrue(result is ResourceData.Success)
        coVerify(exactly = 1) { repository.getProductDetail(productId) }
    }


    @Test
    fun `invoke verifica que se llama al repositorio con parámetros correctos`() = runTest {
        // Given
        val productId = "MCO999888777"
        coEvery { repository.getProductDetail(productId) } returns ResourceData.Success(
            mockProductDetail
        )

        // When
        useCase.invoke(productId)

        // Then - Verificar que se llamó exactamente con ese ID y nada más
        coVerify(exactly = 1) { repository.getProductDetail(productId) }
        confirmVerified(repository) // Confirma que no hubo otras llamadas al mock
    }


    // ERROR CASES - Validaciones

    @Test
    fun `invoke con ID vacío retorna Error sin llamar al repositorio`() = runTest {
        // Given
        val emptyId = ""

        // When
        val result = useCase.invoke(emptyId)

        // Then
        assertTrue(result is ResourceData.Error)
        assertEquals(
            "El ID del producto no puede estar vacío",
            (result as ResourceData.Error).message
        )
        // Verificar que NO se llamó al repositorio
        coVerify(exactly = 0) { repository.getProductDetail(any()) }
    }


    @Test
    fun `invoke con ID con tabs y saltos de línea retorna Error`() = runTest {
        // Given
        val blankId = "\t\n  \r"

        // When
        val result = useCase.invoke(blankId)

        // Then
        assertTrue(result is ResourceData.Error)
        assertEquals(
            "El ID del producto no puede estar vacío",
            (result as ResourceData.Error).message
        )
        coVerify(exactly = 0) { repository.getProductDetail(any()) }
    }


    @Test
    fun `invoke con ID con menos de 3 letras retorna Error formato inválido`() = runTest {
        // Given
        val invalidId = "MC123456789"

        // When
        val result = useCase.invoke(invalidId)

        // Then
        assertTrue(result is ResourceData.Error)
        assertEquals(
            "Formato de ID inválido. Debe ser similar a: MCO123456789",
            (result as ResourceData.Error).message
        )
        coVerify(exactly = 0) { repository.getProductDetail(any()) }
    }


    @Test
    fun `invoke con ID sin números retorna Error formato inválido`() = runTest {
        // Given
        val invalidId = "MCO"

        // When
        val result = useCase.invoke(invalidId)

        // Then
        assertTrue(result is ResourceData.Error)
        assertEquals(
            "Formato de ID inválido. Debe ser similar a: MCO123456789",
            (result as ResourceData.Error).message
        )
        coVerify(exactly = 0) { repository.getProductDetail(any()) }
    }

    @Test
    fun `invoke con ID con caracteres especiales retorna Error formato inválido`() = runTest {
        // Given
        val invalidId = "MCO-123456789"

        // When
        val result = useCase.invoke(invalidId)

        // Then
        assertTrue(result is ResourceData.Error)
        assertEquals(
            "Formato de ID inválido. Debe ser similar a: MCO123456789",
            (result as ResourceData.Error).message
        )
        coVerify(exactly = 0) { repository.getProductDetail(any()) }
    }


    // ERROR CASES - Errores del Repositorio

    @Test
    fun `invoke cuando repositorio retorna Error propaga el error`() = runTest {
        // Given
        val productId = "MCO123456789"
        val errorMessage = "Error de red: No se pudo conectar al servidor"
        coEvery { repository.getProductDetail(productId) } returns ResourceData.Error(errorMessage)

        // When
        val result = useCase.invoke(productId)

        // Then
        assertTrue(result is ResourceData.Error)
        assertEquals(errorMessage, (result as ResourceData.Error).message)
        coVerify(exactly = 1) { repository.getProductDetail(productId) }
    }

    @Test
    fun `invoke cuando repositorio retorna Error con código propaga correctamente`() = runTest {
        // Given
        val productId = "MCO123456789"
        val errorMessage = "Producto no encontrado"
        val errorCode = 404
        coEvery { repository.getProductDetail(productId) } returns
                ResourceData.Error(message = errorMessage, code = errorCode)

        // When
        val result = useCase.invoke(productId)

        // Then
        assertTrue(result is ResourceData.Error)
        val error = result as ResourceData.Error
        assertEquals(errorMessage, error.message)
        assertEquals(errorCode, error.code)
        coVerify(exactly = 1) { repository.getProductDetail(productId) }
    }

    @Test
    fun `invoke cuando repositorio retorna Error con Throwable propaga correctamente`() = runTest {
        // Given
        val productId = "MCO123456789"
        val errorMessage = "Timeout al conectar"
        val throwable = Exception("Connection timeout")
        coEvery { repository.getProductDetail(productId) } returns
                ResourceData.Error(message = errorMessage, cause = throwable)

        // When
        val result = useCase.invoke(productId)

        // Then
        assertTrue(result is ResourceData.Error)
        val error = result as ResourceData.Error
        assertEquals(errorMessage, error.message)
        assertEquals(throwable, error.cause)
        coVerify(exactly = 1) { repository.getProductDetail(productId) }
    }
}