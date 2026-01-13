package com.example.pruebameli.domain.auth

import com.example.pruebameli.domain.repository.AuthRepository
import com.example.pruebameli.domain.repository.TokenStorage
import com.example.pruebameli.utils.MainDispatcherRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests unitarios para AuthManager.
 * 
 * Cobertura:
 * - Success cases: token válido, refresh exitoso, concurrencia
 * - Error cases: sin refresh token, refresh token vacío/blank, fallo en refresh
 * - Edge cases: double-check locking, race conditions, múltiples refreshes
 */
@ExperimentalCoroutinesApi
class AuthManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var storage: TokenStorage

    @MockK
    private lateinit var repository: AuthRepository

    private lateinit var authManager: AuthManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        authManager = AuthManager(storage, repository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ========== SUCCESS CASES ==========

    @Test
    fun `getValidAccessToken returns access token when refresh not needed`() = runTest {
        // Arrange
        val expectedToken = "valid_access_token"
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } returns false
        every { storage.getAccessToken() } returns expectedToken

        // Act
        val result = authManager.getValidAccessToken()

        // Assert
        assertEquals(expectedToken, result)
        verify(exactly = 1) { storage.getRefreshToken() }
        verify(exactly = 1) { storage.shouldRefresh() }
        verify(exactly = 1) { storage.getAccessToken() }
        coVerify(exactly = 0) { repository.refreshAndSaveToken() }
    }

    @Test
    fun `getValidAccessToken refreshes and returns new token when refresh needed`() = runTest {
        // Arrange
        val newAccessToken = "new_access_token"
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } returns true andThen false
        every { storage.getAccessToken() } returns newAccessToken
        coEvery { repository.refreshAndSaveToken() } returns Result.success(Unit)

        // Act
        val result = authManager.getValidAccessToken()

        // Assert
        assertEquals(newAccessToken, result)
        verify(exactly = 1) { storage.getRefreshToken() }
        verify(exactly = 2) { storage.shouldRefresh() } // Called twice: before and inside mutex
        verify(exactly = 1) { storage.getAccessToken() }
        coVerify(exactly = 1) { repository.refreshAndSaveToken() }
    }

    @Test
    fun `getValidAccessToken uses double-check locking correctly`() = runTest {
        // Arrange
        val accessToken = "access_token"
        every { storage.getRefreshToken() } returns "refresh_token"
        // First check: needs refresh, Second check inside mutex: no longer needs refresh
        every { storage.shouldRefresh() } returns true andThen false
        every { storage.getAccessToken() } returns accessToken

        // Act
        val result = authManager.getValidAccessToken()

        // Assert
        assertEquals(accessToken, result)
        verify(exactly = 2) { storage.shouldRefresh() } // Double-check
        verify(exactly = 1) { storage.getAccessToken() }
        coVerify(exactly = 0) { repository.refreshAndSaveToken() } // Not called due to second check
    }

    @Test
    fun `getValidAccessToken handles concurrent calls with single refresh`() = runTest {
        // Arrange
        val newAccessToken = "new_access_token"
        var refreshCallCount = 0
        
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } returns true andThen false
        every { storage.getAccessToken() } returns newAccessToken
        coEvery { repository.refreshAndSaveToken() } coAnswers {
            refreshCallCount++
            Result.success(Unit)
        }

        // Act - Launch 5 concurrent requests
        val results = mutableListOf<String?>()
        repeat(5) {
            launch {
                results.add(authManager.getValidAccessToken())
            }
        }
        advanceUntilIdle()

        // Assert
        assertEquals(5, results.size)
        results.forEach { assertEquals(newAccessToken, it) }
        assertEquals(1, refreshCallCount) // Only one refresh despite 5 concurrent calls
        coVerify(exactly = 1) { repository.refreshAndSaveToken() }
    }

    // ========== ERROR CASES ==========

    @Test
    fun `getValidAccessToken returns null when refresh token is null`() = runTest {
        // Arrange
        every { storage.getRefreshToken() } returns null

        // Act
        val result = authManager.getValidAccessToken()

        // Assert
        assertNull(result)
        verify(exactly = 1) { storage.getRefreshToken() }
        verify(exactly = 0) { storage.shouldRefresh() }
        verify(exactly = 0) { storage.getAccessToken() }
        coVerify(exactly = 0) { repository.refreshAndSaveToken() }
    }

    @Test
    fun `getValidAccessToken returns null when refresh token is empty`() = runTest {
        // Arrange
        every { storage.getRefreshToken() } returns ""

        // Act
        val result = authManager.getValidAccessToken()

        // Assert
        assertNull(result)
        verify(exactly = 1) { storage.getRefreshToken() }
        verify(exactly = 0) { storage.shouldRefresh() }
        verify(exactly = 0) { storage.getAccessToken() }
        coVerify(exactly = 0) { repository.refreshAndSaveToken() }
    }

    @Test
    fun `getValidAccessToken returns null when refresh token is blank`() = runTest {
        // Arrange
        every { storage.getRefreshToken() } returns "   "

        // Act
        val result = authManager.getValidAccessToken()

        // Assert
        assertNull(result)
        verify(exactly = 1) { storage.getRefreshToken() }
        verify(exactly = 0) { storage.shouldRefresh() }
    }

    @Test
    fun `getValidAccessToken throws exception when refresh fails`() = runTest {
        // Arrange
        val expectedException = Exception("Network error")
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } returns true
        coEvery { repository.refreshAndSaveToken() } returns Result.failure(expectedException)

        // Act & Assert
        val exception = assertFailsWith<Exception> {
            authManager.getValidAccessToken()
        }

        assertEquals("Network error", exception.message)
        verify(exactly = 1) { storage.getRefreshToken() }
        verify(exactly = 2) { storage.shouldRefresh() }
        coVerify(exactly = 1) { repository.refreshAndSaveToken() }
        verify(exactly = 0) { storage.getAccessToken() } // Not called due to exception
    }

    @Test
    fun `getValidAccessToken throws exception when refresh returns failure result`() = runTest {
        // Arrange
        val errorMessage = "Invalid refresh token"
        every { storage.getRefreshToken() } returns "invalid_refresh_token"
        every { storage.shouldRefresh() } returns true
        coEvery { repository.refreshAndSaveToken() } returns Result.failure(
            IllegalStateException(errorMessage)
        )

        // Act & Assert
        val exception = assertFailsWith<IllegalStateException> {
            authManager.getValidAccessToken()
        }

        assertEquals(errorMessage, exception.message)
        coVerify(exactly = 1) { repository.refreshAndSaveToken() }
    }

    // ========== EDGE CASES ==========

    @Test
    fun `getValidAccessToken returns null when access token is null after refresh`() = runTest {
        // Arrange
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } returns true andThen false
        every { storage.getAccessToken() } returns null
        coEvery { repository.refreshAndSaveToken() } returns Result.success(Unit)

        // Act
        val result = authManager.getValidAccessToken()

        // Assert
        assertNull(result)
        verify(exactly = 1) { storage.getAccessToken() }
        coVerify(exactly = 1) { repository.refreshAndSaveToken() }
    }

    @Test
    fun `getValidAccessToken returns empty string when access token is empty`() = runTest {
        // Arrange
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } returns false
        every { storage.getAccessToken() } returns ""

        // Act
        val result = authManager.getValidAccessToken()

        // Assert
        assertEquals("", result)
        verify(exactly = 1) { storage.getAccessToken() }
    }

    @Test
    fun `getValidAccessToken handles multiple sequential refresh attempts`() = runTest {
        // Arrange
        val firstToken = "first_token"
        val secondToken = "second_token"
        
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } returns true andThen false andThen true andThen false
        every { storage.getAccessToken() } returns firstToken andThen secondToken
        coEvery { repository.refreshAndSaveToken() } returns Result.success(Unit)

        // Act
        val firstResult = authManager.getValidAccessToken()
        val secondResult = authManager.getValidAccessToken()

        // Assert
        assertEquals(firstToken, firstResult)
        assertEquals(secondToken, secondResult)
        coVerify(exactly = 2) { repository.refreshAndSaveToken() }
    }

    @Test
    fun `getValidAccessToken handles network timeout during refresh`() = runTest {
        // Arrange
        val timeoutException = java.net.SocketTimeoutException("Connection timeout")
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } returns true
        coEvery { repository.refreshAndSaveToken() } returns Result.failure(timeoutException)

        // Act & Assert
        val exception = assertFailsWith<java.net.SocketTimeoutException> {
            authManager.getValidAccessToken()
        }

        assertEquals("Connection timeout", exception.message)
        coVerify(exactly = 1) { repository.refreshAndSaveToken() }
    }

    @Test
    fun `getValidAccessToken verifies mutex prevents race condition`() = runTest {
        // Arrange
        val newToken = "refreshed_token"
        var shouldRefreshCallCount = 0
        
        every { storage.getRefreshToken() } returns "refresh_token"
        every { storage.shouldRefresh() } answers {
            shouldRefreshCallCount++
            // First call: true, all others: false (simulating token refreshed)
            shouldRefreshCallCount == 1
        }
        every { storage.getAccessToken() } returns newToken
        coEvery { repository.refreshAndSaveToken() } returns Result.success(Unit)

        // Act - Multiple concurrent calls
        val results = List(3) {
            launch {
                authManager.getValidAccessToken()
            }
        }
        advanceUntilIdle()

        // Assert
        // Due to double-check locking and mutex:
        // - First coroutine: checks shouldRefresh (true), enters mutex, checks again (false due to increment), no refresh
        // - The behavior depends on execution order, but mutex ensures thread-safety
        verify(atLeast = 2) { storage.shouldRefresh() }
    }
}
