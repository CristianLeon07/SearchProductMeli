# Tests Unitarios - AuthManager

## 📋 Descripción

Tests unitarios completos para `AuthManager` siguiendo las mejores prácticas de Clean Architecture y testing en Android/Kotlin.

## 🎯 Cobertura de Tests

### ✅ Success Cases
- **Token válido sin refresh**: Cuando el token aún es válido y no requiere renovación
- **Refresh exitoso**: Cuando el token expira y se refresca correctamente
- **Double-check locking**: Verifica el patrón de doble verificación con Mutex
- **Concurrencia**: Múltiples llamadas simultáneas con un solo refresh

### ❌ Error Cases
- **Sin refresh token**: Usuario no autenticado (null)
- **Refresh token vacío**: String vacío ("")
- **Refresh token blank**: String con espacios ("   ")
- **Fallo en refresh**: Error de red o API
- **Result.failure**: Manejo de Result<Unit> fallido
- **Timeout**: Simulación de timeout de red

### 🔧 Edge Cases
- **Access token null después de refresh**: Caso extremo post-refresh
- **Access token vacío**: String vacío retornado
- **Múltiples refreshes secuenciales**: Varios refreshes uno tras otro
- **Race conditions**: Verificación de thread-safety con Mutex

## 🏗️ Estructura AAA

Todos los tests siguen el patrón **Arrange-Act-Assert**:

```kotlin
@Test
fun `test description`() = runTest {
    // Arrange - Configuración de mocks y datos
    val expectedToken = "token"
    every { storage.getRefreshToken() } returns "refresh"
    
    // Act - Ejecución de la función
    val result = authManager.getValidAccessToken()
    
    // Assert - Verificación de resultados
    assertEquals(expectedToken, result)
    verify { storage.getRefreshToken() }
}
```

## 🔍 Verificaciones

Cada test verifica:
- ✓ Valor retornado correcto
- ✓ Número exacto de llamadas a cada mock (`exactly = N`)
- ✓ Orden de llamadas cuando es relevante (`andThen`)
- ✓ No se llaman funciones innecesarias (`exactly = 0`)
- ✓ Excepciones lanzadas correctamente

## 🚀 Cómo Ejecutar los Tests

### Desde Android Studio:
1. Abre `AuthManagerTest.kt`
2. Click derecho en la clase → **Run 'AuthManagerTest'**
3. O ejecuta tests individuales con el ícono ▶️ junto a cada `@Test`

### Desde Terminal:
```bash
# Todos los tests unitarios del proyecto
./gradlew test

# Solo tests de AuthManager
./gradlew test --tests "com.example.pruebameli.domain.auth.AuthManagerTest"

# Un test específico
./gradlew test --tests "com.example.pruebameli.domain.auth.AuthManagerTest.getValidAccessToken returns access token when refresh not needed"

# Con reporte HTML
./gradlew test
# Reporte en: app/build/reports/tests/testDebugUnitTest/index.html
```

## 📦 Dependencias Necesarias

Las siguientes dependencias ya están agregadas en `build.gradle.kts`:

```kotlin
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
testImplementation("junit:junit:4.13.2")
```

## 🛠️ Herramientas Utilizadas

- **MockK**: Framework de mocking para Kotlin
- **Coroutines Test**: Utilidades para testear coroutines
- **JUnit 4**: Framework de testing
- **Kotlin Test**: Assertions mejoradas para Kotlin

## 📊 Estadísticas

- **Total de tests**: 14
- **Success cases**: 4 tests
- **Error cases**: 5 tests
- **Edge cases**: 5 tests
- **Cobertura estimada**: ~100% del código de AuthManager

## 💡 Características Destacadas

### 1. MainDispatcherRule
Configura automáticamente el dispatcher de coroutines para tests:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

### 2. Test de Concurrencia
Verifica que el Mutex funciona correctamente con múltiples coroutines:

```kotlin
repeat(5) {
    launch {
        results.add(authManager.getValidAccessToken())
    }
}
advanceUntilIdle()
```

### 3. Verificación de Mutex
Asegura que solo se ejecuta un refresh a la vez, evitando race conditions.

### 4. Sin Dependencias de Android Framework
Todos los tests son **unit tests puros** que no requieren emulador ni dispositivo.

## 📝 Notas Importantes

- Los tests usan `runTest` de coroutines-test para manejo determinístico de coroutines
- `clearAllMocks()` se ejecuta después de cada test para aislamiento
- Los tests son **rápidos** y **determinísticos**
- No hay sleeps ni delays reales
- Todos los tests son independientes entre sí

## 🎓 Buenas Prácticas Implementadas

✅ Patrón AAA (Arrange-Act-Assert)  
✅ Nombres descriptivos en español con backticks  
✅ Un assert por concepto lógico  
✅ Verificación de todos los paths del código  
✅ Testing de casos extremos (edge cases)  
✅ Verificación de llamadas a mocks  
✅ Testing de concurrencia  
✅ Sin dependencias del framework Android  
✅ Tests aislados e independientes  
✅ Setup y teardown apropiados  

## 🔗 Referencias

- [MockK Documentation](https://mockk.io/)
- [Kotlin Coroutines Test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/)
- [JUnit 4](https://junit.org/junit4/)
