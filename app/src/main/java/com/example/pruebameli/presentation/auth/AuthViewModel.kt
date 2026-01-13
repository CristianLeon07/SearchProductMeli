package com.example.pruebameli.presentation.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebameli.domain.config.AppConfig
import com.example.pruebameli.domain.usecase.auth.BuildAuthUrlUseCase
import com.example.pruebameli.domain.usecase.auth.ExchangeCodeForTokenUseCase
import com.example.pruebameli.domain.usecase.auth.GetAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val buildAuthUrl: BuildAuthUrlUseCase,
    private val exchangeCode: ExchangeCodeForTokenUseCase,
    getAuthState: GetAuthStateUseCase
) : ViewModel() {

    /**
     * Estado reactivo: true si existe refresh_token guardado.
     */
    val hasSession: StateFlow<Boolean> =
        getAuthState()
            .stateIn(
                viewModelScope, 
                SharingStarted.WhileSubscribed(AppConfig.Flow.STATE_FLOW_TIMEOUT_MS), 
                false
            )

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    /**
     * Evento one-shot: abrir página de auth
     */
    private val _openAuthPage = MutableSharedFlow<Uri>(
        extraBufferCapacity = AppConfig.UI.EVENT_BUFFER_CAPACITY
    )
    val openAuthPage: SharedFlow<Uri> = _openAuthPage.asSharedFlow()

    /**
     * Evento one-shot: navegación / acciones de UI
     */
    private val _events = MutableSharedFlow<AuthEvent>(
        extraBufferCapacity = AppConfig.UI.EVENT_BUFFER_CAPACITY
    )
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun onLoginClick() {
        val request = buildAuthUrl()
        _openAuthPage.tryEmit(request.url)
    }

    fun onAuthCodeReceived(code: String?) {
        if (code.isNullOrBlank()) {
            _state.value = AuthUiState.Error("No llegó el code en el redirect.")
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState.Loading

            exchangeCode(code).fold(
                onSuccess = {
                    _state.value = AuthUiState.Success("Autenticación exitosa")

                    //Navegar a Home tras autenticar
                    _events.tryEmit(AuthEvent.NavigateHome)
                },
                onFailure = { e ->
                    _state.value = AuthUiState.Error(e.message ?: "Error desconocido")
                }
            )
        }
    }
}
