package com.example.pruebameli.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.pruebameli.domain.network.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de NetworkMonitor usando ConnectivityManager y NetworkCallback.
 *
 * Esta clase monitorea en tiempo real el estado de conectividad de red
 * usando las APIs modernas de Android (no deprecated).
 *
 * Características:
 * - Usa NetworkCallback para recibir actualizaciones de conectividad
 * - Emite el estado actual inmediatamente al suscribirse
 * - Se limpia automáticamente cuando no hay colectores (callbackFlow)
 * - Singleton para compartir el mismo callback entre múltiples suscriptores
 *
 * @param context ApplicationContext inyectado por Hilt (no se filtra a ViewModels)
 */
@Singleton
class NetworkMonitorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkMonitor {

    companion object {
        private const val TAG = "NETWORK_MONITOR"
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Flow que emite el estado actual de conectividad.
     *
     * Funcionamiento:
     * 1. callbackFlow permite crear un Flow a partir de callbacks
     * 2. Registra un NetworkCallback cuando se inicia la colección
     * 3. Emite `true` cuando hay red disponible con capacidades de internet
     * 4. Emite `false` cuando se pierde la conexión
     * 5. Se desregistra automáticamente con awaitClose
     * 6. distinctUntilChanged evita emisiones duplicadas
     */
    override val isConnected: Flow<Boolean> = callbackFlow {
        Log.d(TAG, "🔌 NetworkMonitor iniciado - Registrando callback")

        val networkCallback = object : ConnectivityManager.NetworkCallback() {

            // Redes actualmente disponibles (puede haber múltiples: WiFi + datos móviles)
            private val availableNetworks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                Log.d(TAG, "Red disponible: $network")
                availableNetworks.add(network)
                // Hay al menos una red disponible
                trySend(true)
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Red perdida: $network")
                availableNetworks.remove(network)
                // Si no quedan redes disponibles, emitir false
                if (availableNetworks.isEmpty()) {
                    Log.w(TAG, "Sin conexión a internet")
                    trySend(false)
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                // Verifica que la red tenga capacidad de INTERNET validado
                val hasInternet = capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )

                Log.v(TAG, "Capacidades de red cambiadas - Internet validado: $hasInternet")

                if (hasInternet) {
                    availableNetworks.add(network)
                    trySend(true)
                } else {
                    availableNetworks.remove(network)
                    if (availableNetworks.isEmpty()) {
                        trySend(false)
                    }
                }
            }
        }

        // Construye la solicitud de red para monitorear solo redes con internet
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        // Registra el callback
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Emite el estado inicial basándose en la red activa actual
        val currentNetwork = connectivityManager.activeNetwork
        val hasConnection = currentNetwork != null &&
                connectivityManager.getNetworkCapabilities(currentNetwork)?.let { capabilities ->
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                } ?: false

        Log.d(TAG, "Estado inicial de conexión: $hasConnection")
        trySend(hasConnection)

        // Cuando el Flow se cierra (no hay más colectores), desregistra el callback
        awaitClose {
            Log.d(TAG, "NetworkMonitor cerrado - Desregistrando callback")
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged() // Evita emitir el mismo valor consecutivamente
}