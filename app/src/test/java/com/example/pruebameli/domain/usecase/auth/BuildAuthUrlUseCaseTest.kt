package com.example.pruebameli.domain.usecase.auth

import androidx.core.net.toUri
import android.net.Uri
import com.example.pruebameli.core.config.MeliAuthConfig
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class BuildAuthUrlUseCaseTest {

    private lateinit var useCase: BuildAuthUrlUseCase

    @Before
    fun setUp() {
        // MockK para funciones estáticas de Android
        mockkStatic(Uri::class)
        mockkStatic("androidx.core.net.UriKt")

        useCase = BuildAuthUrlUseCase()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }


    @Test
    fun `invoke should return AuthRequest with valid URL and state`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act
        val result = useCase()

        // Assert
        assertNotNull("AuthRequest should not be null", result)
        assertNotNull("URL should not be null", result.url)
        assertNotNull("State should not be null", result.state)
        assertFalse("State should not be empty", result.state.isEmpty())
    }

    @Test
    fun `invoke should generate valid UUID format for state`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act
        val result = useCase()

        // Assert - Verificar que el state tiene formato UUID válido
        try {
            UUID.fromString(result.state)
            assertTrue("State should be a valid UUID", true)
        } catch (e: IllegalArgumentException) {
            fail("State should be a valid UUID format but got: ${result.state}")
        }
    }

    @Test
    fun `invoke should append all required OAuth query parameters`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)
        val capturedParams = mutableMapOf<String, String>()

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every {
            mockUriBuilder.appendQueryParameter(capture(slot<String>()), capture(slot<String>()))
        } answers {
            capturedParams[firstArg()] = secondArg()
            mockUriBuilder
        }
        every { mockUriBuilder.build() } returns finalUri

        // Act
        val result = useCase()

        // Assert - Verificar que todos los parámetros requeridos fueron agregados
        verify(exactly = 4) { mockUriBuilder.appendQueryParameter(any(), any()) }

        assertTrue(
            "Should contain response_type parameter",
            capturedParams.containsKey("response_type")
        )
        assertTrue(
            "Should contain client_id parameter",
            capturedParams.containsKey("client_id")
        )
        assertTrue(
            "Should contain redirect_uri parameter",
            capturedParams.containsKey("redirect_uri")
        )
        assertTrue(
            "Should contain state parameter",
            capturedParams.containsKey("state")
        )
    }

    @Test
    fun `invoke should use correct OAuth parameter values`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)
        val capturedParams = mutableMapOf<String, String>()

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every {
            mockUriBuilder.appendQueryParameter(capture(slot<String>()), capture(slot<String>()))
        } answers {
            capturedParams[firstArg()] = secondArg()
            mockUriBuilder
        }
        every { mockUriBuilder.build() } returns finalUri

        // Act
        val result = useCase()

        // Assert - Verificar valores correctos de cada parámetro
        assertEquals(
            "response_type should be 'code'",
            "code", capturedParams["response_type"]
        )
        assertEquals(
            "client_id should match config",
            MeliAuthConfig.CLIENT_ID, capturedParams["client_id"]
        )
        assertEquals(
            "redirect_uri should match config",
            MeliAuthConfig.REDIRECT_URI, capturedParams["redirect_uri"]
        )
        assertNotNull(
            "state should not be null",
            capturedParams["state"]
        )
        assertEquals(
            "state in URL should match state in result",
            result.state, capturedParams["state"]
        )
    }

    @Test
    fun `invoke should use correct base URL from config`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)
        val baseUrlSlot = slot<String>()

        every { capture(baseUrlSlot).toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act
        useCase()

        // Assert
        assertEquals(
            "Should use AUTH_BASE_URL from config",
            MeliAuthConfig.AUTH_BASE_URL, baseUrlSlot.captured
        )
    }

    // EDGE CASES

    @Test
    fun `invoke should generate unique state on each invocation`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act - Invocar múltiples veces
        val result1 = useCase()
        val result2 = useCase()
        val result3 = useCase()

        // Assert - Cada state debe ser único
        assertNotEquals(
            "First and second invocation should generate different states",
            result1.state, result2.state
        )
        assertNotEquals(
            "Second and third invocation should generate different states",
            result2.state, result3.state
        )
        assertNotEquals(
            "First and third invocation should generate different states",
            result1.state, result3.state
        )
    }

    @Test
    fun `invoke should generate state with standard UUID length`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act
        val result = useCase()

        // Assert - UUID en formato string tiene 36 caracteres (8-4-4-4-12 con guiones)
        assertEquals(
            "UUID state should have standard length",
            36, result.state.length
        )
        assertTrue(
            "UUID should contain hyphens in correct positions",
            result.state.matches(Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"))
        )
    }

    @Test
    fun `invoke should maintain state consistency between AuthRequest fields and URL`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)
        var stateInUrl: String? = null

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every {
            mockUriBuilder.appendQueryParameter(any(), any())
        } answers {
            if (firstArg<String>() == "state") {
                stateInUrl = secondArg()
            }
            mockUriBuilder
        }
        every { mockUriBuilder.build() } returns finalUri

        // Act
        val result = useCase()

        // Assert - El state en el objeto debe ser el mismo que se puso en la URL
        assertNotNull("State in URL should not be null", stateInUrl)
        assertEquals(
            "State in AuthRequest should match state in URL",
            result.state, stateInUrl
        )
    }

    @Test
    fun `invoke should append parameters in correct order`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)
        val paramOrder = mutableListOf<String>()

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every {
            mockUriBuilder.appendQueryParameter(capture(slot<String>()), any())
        } answers {
            paramOrder.add(firstArg())
            mockUriBuilder
        }
        every { mockUriBuilder.build() } returns finalUri

        // Act
        useCase()

        // Assert - Verificar orden esperado
        assertEquals(
            "First parameter should be response_type",
            "response_type", paramOrder[0]
        )
        assertEquals(
            "Second parameter should be client_id",
            "client_id", paramOrder[1]
        )
        assertEquals(
            "Third parameter should be redirect_uri",
            "redirect_uri", paramOrder[2]
        )
        assertEquals(
            "Fourth parameter should be state",
            "state", paramOrder[3]
        )
    }

    @Test
    fun `invoke should not throw exception during normal execution`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act & Assert - No debe lanzar excepción
        try {
            val result = useCase()
            assertNotNull("Result should not be null", result)
        } catch (e: Exception) {
            fail("Should not throw exception but got: ${e.message}")
        }
    }

    // VERIFICATION TESTS

    @Test
    fun `invoke should call toUri extension function on base URL`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act
        useCase()

        // Assert - Verificar que se llamó toUri() exactamente una vez
        verify(exactly = 1) { any<String>().toUri() }
    }

    @Test
    fun `invoke should call buildUpon on Uri exactly once`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act
        useCase()

        // Assert
        verify(exactly = 1) { mockUri.buildUpon() }
    }

    @Test
    fun `invoke should call build on UriBuilder exactly once`() {
        // Arrange
        val mockUri = mockk<Uri>(relaxed = true)
        val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)
        val finalUri = mockk<Uri>(relaxed = true)

        every { any<String>().toUri() } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns finalUri

        // Act
        useCase()

        // Assert
        verify(exactly = 1) { mockUriBuilder.build() }
    }
}