# 🔌 Guía: Validación de Conexión a Internet

## 📋 Resumen

Se ha implementado un sistema completo de validación de conexión a internet siguiendo **Clean Architecture** y las mejores prácticas de Android moderno.

---

## 🏗️ Arquitectura Implementada

### 1️⃣ **Domain Layer** (Interface)
```
domain/network/NetworkMonitor.kt
```
- **Responsabilidad**: Define el contrato de observación de red
- **Sin dependencias de Android**: Facilita testing
- **Expone**: `Flow<Boolean>` con el estado de conectividad

### 2️⃣ **Data Layer** (Implementación)
```
data/network/NetworkMonitorImpl.kt
```
- **Usa APIs modernas**: `NetworkCallback` + `ConnectivityManager`
- **No deprecated**: Compatible con Android 21+
- **Singleton**: Comparte el mismo callback entre suscriptores
- **Características**:
  - ✅ Valida que la red tenga capacidad de internet (`NET_CAPABILITY_VALIDATED`)
  - ✅ Emite estado inicial inmediatamente
  - ✅ Se limpia automáticamente con `callbackFlow`
  - ✅ Evita emisiones duplicadas con `distinctUntilChanged`

### 3️⃣ **DI Layer** (Inyección de dependencias)
```
core/di/AppModule.kt
```
- **Binds**: `NetworkMonitorImpl` → `NetworkMonitor`
- **Scope**: Singleton
- **Beneficio**: Los ViewModels inyectan la interfaz, no la implementación

### 4️⃣ **Presentation Layer** (UI)
```
presentation/components/NoInternetView.kt
```
- **Componente Compose reutilizable**
- **Características**:
  - 🎨 Ilustración de "sin conexión"
  - 📝 Mensaje descriptivo personalizable
  - 🔄 Botón de reintentar (opcional)
  - 📱 Diseño responsive y centrado

---

## 🚀 Cómo Usar en ViewModels

### Ejemplo 1: HomeViewModel (Ya implementado)

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchProductsPaged: SearchProductsPagedUseCase,
    networkMonitor: NetworkMonitor  // ✅ Inyección de la interfaz
) : ViewModel() {

    // Exponer como StateFlow para Compose
    val isConnected: StateFlow<Boolean> = networkMonitor.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Asume conectado inicialmente
        )
    
    // ... resto del código
}
```

### Ejemplo 2: Verificar antes de hacer operaciones

```kotlin
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductDetail: GetProductDetailUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val isConnected = networkMonitor.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun loadProductDetail(id: String) {
        viewModelScope.launch {
            // ✅ Verificar conexión antes de hacer la petición
            if (!isConnected.value) {
                _uiState.value = UiState.NoInternet
                return@launch
            }
            
            // Proceder con la petición
            _uiState.value = UiState.Loading
            getProductDetail(id).collect { result ->
                // ... manejar resultado
            }
        }
    }
}
```

### Ejemplo 3: Reaccionar a cambios de conectividad

```kotlin
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    init {
        // Observar cambios y reaccionar automáticamente
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                if (isConnected) {
                    Log.d(TAG, "Conexión restaurada - Sincronizando...")
                    syncPendingData()
                } else {
                    Log.w(TAG, "Sin conexión - Sincronización pausada")
                }
            }
        }
    }
}
```

---

## 🎨 Integración en Compose

### Opción 1: Mostrar vista completa de "Sin Internet"

```kotlin
@Composable
fun HomeScreen(
    isConnected: Boolean,
    // ... otros parámetros
) {
    if (!isConnected) {
        NoInternetView(
            onRetryClick = { /* acción de reintento */ }
        )
    } else {
        // UI normal
    }
}
```

### Opción 2: Banner superior (no intrusivo)

```kotlin
@Composable
fun ProductListScreen(isConnected: Boolean) {
    Column {
        // Banner de advertencia
        AnimatedVisibility(visible = !isConnected) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sin conexión a internet",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Contenido principal
        ProductList()
    }
}
```

### Opción 3: Snackbar temporal

```kotlin
@Composable
fun MyScreen(isConnected: Boolean) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            snackbarHostState.showSnackbar(
                message = "Sin conexión a internet",
                duration = SnackbarDuration.Indefinite
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        // Contenido
    }
}
```

---

## 🧪 Testing

### Mock para Tests

```kotlin
class FakeNetworkMonitor : NetworkMonitor {
    private val _isConnected = MutableStateFlow(true)
    override val isConnected: Flow<Boolean> = _isConnected
    
    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}

@Test
fun `when no internet, shows error state`() = runTest {
    val fakeNetworkMonitor = FakeNetworkMonitor()
    val viewModel = HomeViewModel(
        searchProductsPaged = fakeSearchUseCase,
        networkMonitor = fakeNetworkMonitor
    )
    
    // Simular pérdida de conexión
    fakeNetworkMonitor.setConnected(false)
    
    // Verificar estado
    assertFalse(viewModel.isConnected.value)
}
```

---

## 📱 Comportamiento en la App

### ✅ Con Conexión
- La app funciona normalmente
- El usuario puede buscar productos
- Las imágenes se cargan correctamente

### ❌ Sin Conexión
1. **Detección automática**: `NetworkMonitor` emite `false`
2. **ViewModel reacciona**: Actualiza `isConnected` StateFlow
3. **UI cambia**: Muestra `NoInternetView` con:
   - Ilustración de "sin conexión"
   - Mensaje: "Por favor, verifica tu conexión e intenta nuevamente."
   - Botón "Reintentar"
4. **Al reconectar**: La UI vuelve automáticamente al estado normal

---

## ⚡ Optimizaciones

### 1. `SharingStarted.WhileSubscribed(5000)`
- El Flow se mantiene activo 5 segundos después de que el último colector se desuscribe
- Evita reinicios del NetworkCallback en rotaciones rápidas
- Ahorra batería cuando la pantalla está en background

### 2. `distinctUntilChanged()`
- Evita emisiones duplicadas consecutivas
- Reduce recomposiciones innecesarias en Compose

### 3. Singleton
- Una sola instancia de `NetworkMonitorImpl`
- Un solo `NetworkCallback` registrado
- Múltiples ViewModels pueden observar el mismo Flow

---

## 🛠️ Personalización

### Cambiar mensaje de NoInternetView

```kotlin
NoInternetView(
    title = "Ups, sin internet",
    message = "Revisa tu WiFi o datos móviles e inténtalo de nuevo.",
    onRetryClick = { /* ... */ }
)
```

### Ocultar botón de reintentar

```kotlin
NoInternetView(
    showRetryButton = false,
    message = "La conexión se restablecerá automáticamente."
)
```

### Agregar ilustración personalizada

Reemplaza `R.drawable.ic_launcher_foreground` en `NoInternetView.kt` con tu propio drawable:

```kotlin
Image(
    painter = painterResource(id = R.drawable.ic_no_internet),
    contentDescription = "Sin conexión a internet",
    modifier = Modifier.size(160.dp)
)
```

---

## 📋 Permisos Requeridos

Ya están declarados en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 🎯 Ventajas de esta Implementación

✅ **Clean Architecture**: Separación clara de responsabilidades  
✅ **Testeable**: Interface fácil de mockear  
✅ **No deprecated**: APIs modernas de Android  
✅ **Sin Context en ViewModels**: Inyectado correctamente por Hilt  
✅ **MVVM puro**: ViewModel expone StateFlow, UI observa  
✅ **Reactive**: Cambios automáticos sin polling  
✅ **Eficiente**: Singleton con cleanup automático  
✅ **Reutilizable**: Componente UI genérico  
✅ **Escalable**: Fácil de extender a otros screens  

---

## 🚨 Notas Importantes

1. **Validación de internet real**: `NET_CAPABILITY_VALIDATED` asegura que hay conectividad real, no solo una red conectada sin internet.

2. **Estado inicial optimista**: Por defecto asume conexión (`initialValue = true`) para evitar mostrar error innecesario al iniciar.

3. **No bloquea operaciones**: La validación es informativa. Las peticiones HTTP fallarán naturalmente si no hay conexión, pero el usuario tendrá feedback visual previo.

4. **Batería**: El NetworkCallback es eficiente y no consume batería significativa.

---

## 📚 Recursos Adicionales

- [Android Network Connectivity](https://developer.android.com/training/monitoring-device-state/connectivity-status-type)
- [NetworkCallback API](https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback)
- [Kotlin Flows](https://kotlinlang.org/docs/flow.html)

---

**¡Implementación completada! 🎉**
