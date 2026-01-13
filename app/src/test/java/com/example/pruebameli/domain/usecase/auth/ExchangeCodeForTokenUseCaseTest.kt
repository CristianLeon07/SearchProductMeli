package com.example.pruebameli.domain.usecase.auth


import com.example.pruebameli.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class ExchangeCodeForTokenUseCaseTest {

    private lateinit var repository: AuthRepository

    private lateinit var useCase: ExchangeCodeForTokenUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ExchangeCodeForTokenUseCase(repository)
    }

    @Test
    fun `invoke con código válido debe retornar success`() = runTest {
        // Given
        val validCode = "valid_auth_code_123"
        coEvery { repository.exchangeCodeAndSaveToken(validCode) } returns Result.success(Unit)

        // When
        val result = useCase(validCode)

        // Then
        assertTrue("El resultado debe ser exitoso", result.isSuccess)
        coVerify(exactly = 1) { repository.exchangeCodeAndSaveToken(validCode) }
    }

    @Test
    fun `invoke debe llamar al repositorio con el código exacto proporcionado`() = runTest {
        // Given
        val code = "specific_code_xyz"
        coEvery { repository.exchangeCodeAndSaveToken(code) } returns Result.success(Unit)

        // When
        useCase(code)

        // Then
        coVerify(exactly = 1) { repository.exchangeCodeAndSaveToken(code) }
    }

    //ERROR CASES

    @Test
    fun `invoke con código vacío debe retornar failure sin llamar al repositorio`() = runTest {
        // Given
        val emptyCode = ""

        // When
        val result = useCase(emptyCode)

        // Then
        assertTrue("El resultado debe ser failure", result.isFailure)
        result.onFailure { exception ->
            assertTrue(
                "Debe ser IllegalArgumentException",
                exception is IllegalArgumentException
            )
            assertEquals(
                "El código de autorización no puede estar vacío",
                exception.message
            )
        }

        // Verificar que NO se llamó al repositorio
        coVerify(exactly = 0) { repository.exchangeCodeAndSaveToken(any()) }
    }

    @Test
    fun `invoke cuando repositorio falla con error de autenticación debe propagarlo`() = runTest {
        // Given
        val code = "invalid_code"
        val authError = IllegalStateException("Invalid authorization code")
        coEvery {
            repository.exchangeCodeAndSaveToken(code)
        } returns Result.failure(authError)

        // When
        val result = useCase(code)

        // Then
        assertTrue("El resultado debe ser failure", result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is IllegalStateException)
            assertEquals("Invalid authorization code", exception.message)
        }
        coVerify(exactly = 1) { repository.exchangeCodeAndSaveToken(code) }
    }

    @Test
    fun `invoke con código que contiene caracteres especiales debe llamar al repositorio`() =
        runTest {
            // Given
            val specialCode = "code!@#$%^&*()_+-=[]{}|;:',.<>?/"
            coEvery { repository.exchangeCodeAndSaveToken(specialCode) } returns Result.success(Unit)

            // When
            val result = useCase(specialCode)

            // Then
            assertTrue("El resultado debe ser exitoso", result.isSuccess)
            coVerify(exactly = 1) { repository.exchangeCodeAndSaveToken(specialCode) }
        }

    @Test
    fun `invoke múltiples veces con el mismo código debe llamar al repositorio cada vez`() =
        runTest {
            // Given
            val code = "same_code"
            coEvery { repository.exchangeCodeAndSaveToken(code) } returns Result.success(Unit)

            // When
            useCase(code)
            useCase(code)
            useCase(code)

            // Then
            coVerify(exactly = 3) { repository.exchangeCodeAndSaveToken(code) }
        }
}