package com.example.pruebameli.domain.usecase.auth



import com.example.pruebameli.domain.repository.TokenStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para GetAuthStateUseCase.
 *
 * Verifica:
 * - Emisión correcta del estado de autenticación
 * - Delegación correcta al TokenStorage
 * - Manejo de diferentes escenarios (autenticado/no autenticado)
 */
class GetAuthStateUseCaseTest {

    // Mocks
    private lateinit var mockStorage: TokenStorage

    // System Under Test
    private lateinit var useCase: GetAuthStateUseCase

    @Before
    fun setup() {
        mockStorage = mockk()
        useCase = GetAuthStateUseCase(mockStorage)
    }


    // SUCCESS CASES

    @Test
    fun `invoke WHEN user is authenticated THEN emits true`() = runTest {
        // Given: TokenStorage retorna Flow con true (usuario autenticado)
        every { mockStorage.isUserAuthenticatedOnceFlow() } returns flowOf(true)

        // When: invocamos el UseCase
        val result = useCase().first()

        // Then: debe emitir true
        assertTrue(result)

        // Verify: debe llamar al método correcto del storage exactamente una vez
        verify(exactly = 1) { mockStorage.isUserAuthenticatedOnceFlow() }
    }

    @Test
    fun `invoke WHEN user is not authenticated THEN emits false`() = runTest {
        // Given: TokenStorage retorna Flow con false (usuario no autenticado)
        every { mockStorage.isUserAuthenticatedOnceFlow() } returns flowOf(false)

        // When: invocamos el UseCase
        val result = useCase().first()

        // Then: debe emitir false
        assertFalse(result)

        // Verify: debe llamar al método correcto del storage
        verify(exactly = 1) { mockStorage.isUserAuthenticatedOnceFlow() }
    }


    // EDGE CASES - Múltiples emisiones

    @Test
    fun `invoke WHEN storage emits multiple values THEN all values are emitted`() = runTest {
        // Given: TokenStorage emite múltiples valores (cambio de estado)
        // Simula: usuario se autentica → false → true
        every { mockStorage.isUserAuthenticatedOnceFlow() } returns flowOf(false, true, true, false)

        // When: colectamos todos los valores del Flow
        val results = useCase().toList()

        // Then: debe emitir todos los valores en orden
        assertEquals(4, results.size)
        assertEquals(listOf(false, true, true, false), results)

        // Verify: debe llamar al storage
        verify(exactly = 1) { mockStorage.isUserAuthenticatedOnceFlow() }
    }

    @Test
    fun `invoke WHEN auth state changes from false to true THEN emits both values`() = runTest {
        // Given: Simula usuario que inicia sesión durante la observación
        every { mockStorage.isUserAuthenticatedOnceFlow() } returns flowOf(false, true)

        // When: colectamos los valores
        val results = useCase().toList()

        // Then: primero false (no autenticado), luego true (autenticado)
        assertEquals(2, results.size)
        assertFalse(results[0])
        assertTrue(results[1])

        verify(exactly = 1) { mockStorage.isUserAuthenticatedOnceFlow() }
    }

    @Test
    fun `invoke WHEN auth state changes from true to false THEN emits both values`() = runTest {
        // Given: Simula cierre de sesión durante la observación
        every { mockStorage.isUserAuthenticatedOnceFlow() } returns flowOf(true, false)

        // When: colectamos los valores
        val results = useCase().toList()

        // Then: primero true (autenticado), luego false (cerró sesión)
        assertEquals(2, results.size)
        assertTrue(results[0])
        assertFalse(results[1])

        verify(exactly = 1) { mockStorage.isUserAuthenticatedOnceFlow() }
    }


    // EDGE CASES - Valores consistentes

    @Test
    fun `invoke WHEN called multiple times THEN delegates to storage each time`() = runTest {
        // Given: Storage siempre retorna true
        every { mockStorage.isUserAuthenticatedOnceFlow() } returns flowOf(true)

        // When: invocamos el UseCase múltiples veces
        val result1 = useCase().first()
        val result2 = useCase().first()
        val result3 = useCase().first()

        // Then: todos deben retornar el mismo valor
        assertTrue(result1)
        assertTrue(result2)
        assertTrue(result3)

        // Verify: debe llamar al storage 3 veces (una por cada invocación)
        verify(exactly = 3) { mockStorage.isUserAuthenticatedOnceFlow() }
    }

    @Test
    fun `invoke WHEN storage returns same value multiple times THEN emits all occurrences`() =
        runTest {
            // Given: Storage emite el mismo valor repetidas veces (sin cambio de estado)
            every { mockStorage.isUserAuthenticatedOnceFlow() } returns flowOf(true, true, true)

            // When: colectamos todos los valores
            val results = useCase().toList()

            // Then: debe emitir todos los valores aunque sean iguales
            assertEquals(3, results.size)
            assertTrue(results.all { it == true })

            verify(exactly = 1) { mockStorage.isUserAuthenticatedOnceFlow() }
        }


    @Test
    fun `invoke THEN returns Flow directly from storage without transformation`() = runTest {
        // Given: Storage retorna un Flow específico
        val expectedFlow = flowOf(true)
        every { mockStorage.isUserAuthenticatedOnceFlow() } returns expectedFlow

        // When: obtenemos el Flow del UseCase
        val resultFlow = useCase()

        // Then: debe retornar exactamente el mismo Flow (sin transformaciones)
        val result = resultFlow.first()
        assertTrue(result)

        // Verify: solo debe leer el Flow, sin mapeos ni transformaciones
        verify(exactly = 1) { mockStorage.isUserAuthenticatedOnceFlow() }
    }
}