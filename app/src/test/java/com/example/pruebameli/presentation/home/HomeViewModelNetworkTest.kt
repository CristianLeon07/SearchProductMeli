package com.example.pruebameli.presentation.home

import app.cash.turbine.test
import com.example.pruebameli.domain.network.FakeNetworkMonitor
import com.example.pruebameli.domain.usecase.SearchProductsPagedUseCase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para verificar el comportamiento de HomeViewModel
 * con diferentes estados de conectividad de red.
 * 
 * Estos tests demuestran:
 * - Cómo mockear NetworkMonitor usando FakeNetworkMonitor
 * - Cómo verificar que el ViewModel reacciona correctamente a cambios de red
 * - Cómo usar Turbine para testear Flows
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelNetworkTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var fakeNetworkMonitor: FakeNetworkMonitor
    private lateinit var mockSearchUseCase: SearchProductsPagedUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        fakeNetworkMonitor = FakeNetworkMonitor()
        mockSearchUseCase = mockk(relaxed = true)
        
        viewModel = HomeViewModel(
            searchProductsPaged = mockSearchUseCase,
            networkMonitor = fakeNetworkMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is connected`() = runTest {
        // Given: NetworkMonitor inicia con conexión (default)
        
        // When: Se observa el estado inicial
        val isConnected = viewModel.isConnected.value
        
        // Then: El estado debe ser conectado
        assertTrue("Estado inicial debe ser conectado", isConnected)
    }

    @Test
    fun `when network is lost, isConnected emits false`() = runTest {
        // Given: ViewModel con conexión inicial
        viewModel.isConnected.test {
            // Consumir el valor inicial (true)
            assertTrue(awaitItem())
            
            // When: Se pierde la conexión
            fakeNetworkMonitor.setConnected(false)
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Then: Debe emitir false
            assertFalse(awaitItem())
            
            cancel()
        }
    }

    @Test
    fun `when network is restored, isConnected emits true`() = runTest {
        // Given: Sin conexión inicial
        fakeNetworkMonitor.setConnected(false)
        
        viewModel.isConnected.test {
            // Consumir estado inicial (false)
            assertFalse(awaitItem())
            
            // When: Se restaura la conexión
            fakeNetworkMonitor.setConnected(true)
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Then: Debe emitir true
            assertTrue(awaitItem())
            
            cancel()
        }
    }

    @Test
    fun `network state changes are reflected in viewModel`() = runTest {
        viewModel.isConnected.test {
            // Estado inicial
            assertTrue("Debe iniciar conectado", awaitItem())
            
            // Perder conexión
            fakeNetworkMonitor.setConnected(false)
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse("Debe cambiar a desconectado", awaitItem())
            
            // Recuperar conexión
            fakeNetworkMonitor.setConnected(true)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue("Debe volver a conectado", awaitItem())
            
            // Perder conexión nuevamente
            fakeNetworkMonitor.setConnected(false)
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse("Debe cambiar a desconectado otra vez", awaitItem())
            
            cancel()
        }
    }
}
