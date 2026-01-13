# Análisis de Clean Architecture - Proyecto Prueba Meli

## 📋 Resumen Ejecutivo

Tu proyecto tiene una **buena base** de Clean Architecture, pero hay varias **violaciones importantes** de los principios de dependencias entre capas que necesitan corregirse.

---

## 🔴 Violaciones Críticas de Clean Architecture

### 1. **Domain depende de Core.utils.ResourceData** ⚠️ CRÍTICO

**Ubicación:** `domain/repository/ProductsRepository.kt:4`

**Problema:**
```kotlin
import com.example.pruebameli.core.utils.ResourceData  // ❌ Domain depende de Core
```

En Clean Architecture, **Domain no debe depender de ninguna otra capa**. `ResourceData` está en `core.utils`, pero el repositorio del dominio lo usa.

**Solución:**
- Mover `ResourceData` a `domain/common/` o `domain/utils/`
- Domain puede tener sus propias clases de resultado/error
- Alternativa: Usar `Result<T>` de Kotlin estándar en lugar de ResourceData

**Prioridad:** 🔴 **ALTA** - Viola el principio fundamental de Clean Architecture

---

### 2. **Domain.auth.AuthManager depende de Data.auth.AuthRepository** ⚠️ CRÍTICO

**Ubicación:** `domain/auth/AuthManager.kt:4`

**Problema:**
```kotlin
import com.example.pruebameli.data.auth.AuthRepository  // ❌ Domain depende de Data
```

`AuthManager` está en Domain pero depende directamente de `AuthRepository` que está en Data. Esto es una violación grave.

**Solución:**
1. Crear interfaz `AuthRepository` en `domain/repository/`
2. Mover `AuthRepository` de Data a ser una implementación que implemente la interfaz de Domain
3. `AuthManager` debe depender solo de la interfaz del Domain

**Estructura sugerida:**
```
domain/repository/AuthRepository.kt (interfaz)
data/auth/AuthRepositoryImpl.kt (implementación)
```

**Prioridad:** 🔴 **ALTA** - Viola el principio fundamental de Clean Architecture

---

### 3. **Domain.auth.AuthManager depende de Core.storage.TokenStorage** ⚠️ CRÍTICO

**Ubicación:** `domain/auth/AuthManager.kt:3`

**Problema:**
```kotlin
import com.example.pruebameli.core.storage.TokenStorage  // ❌ Domain depende de Core (Android)
```

`TokenStorage` es una implementación concreta de Android (usa DataStore). Domain no debe depender de implementaciones concretas de frameworks.

**Solución:**
1. Crear interfaz `TokenStorage` en `domain/repository/` o `domain/auth/`
2. `TokenStorage` de `core.storage` debe implementar esta interfaz
3. `AuthManager` debe depender solo de la interfaz

**Estructura sugerida:**
```
domain/repository/TokenStorage.kt (interfaz)
core/storage/TokenStorageImpl.kt (implementación con DataStore)
```

**Prioridad:** 🔴 **ALTA** - Viola el principio de independencia de Domain

---

### 4. **Presentation depende directamente de Data.auth.AuthRepository** ⚠️ MEDIA

**Ubicación:** `presentation/auth/AuthViewModel.kt:7`

**Problema:**
```kotlin
import com.example.pruebameli.data.auth.AuthRepository  // ❌ Presentation depende de Data
```

`AuthViewModel` debería usar UseCases de Domain, no repositorios directamente.

**Solución:**
- Crear `LoginUseCase` y `ExchangeCodeUseCase` en `domain/usecase/auth/`
- `AuthViewModel` debe usar estos UseCases
- Los UseCases usan la interfaz `AuthRepository` de Domain

**Prioridad:** 🟡 **MEDIA** - Afecta separación de responsabilidades

---

### 5. **Presentation depende de Core.storage.TokenStorage** ⚠️ MEDIA

**Ubicación:** `presentation/auth/AuthViewModel.kt:6`

**Problema:**
```kotlin
import com.example.pruebameli.core.storage.TokenStorage  // ❌ Presentation depende de Core
```

`AuthViewModel` accede directamente a `TokenStorage` para obtener el estado de autenticación.

**Solución:**
- Crear `GetAuthStateUseCase` en Domain que use la interfaz `TokenStorage`
- `AuthViewModel` usa el UseCase en lugar de acceder directamente

**Prioridad:** 🟡 **MEDIA** - Afecta separación de responsabilidades

---

## 🟡 Mejoras Importantes

### 6. **UseCase con valores hardcodeados**

**Ubicación:** `domain/usecase/SearchProductsPagedUseCase.kt:17-21`

**Problema:**
```kotlin
siteId = "MCO",  // ❌ Hardcoded
status = "active",  // ❌ Hardcoded
limit = 20,  // ❌ Hardcoded
```

Los valores por defecto deberían venir de parámetros o configuración.

**Solución:**
- Crear objeto de configuración `SearchConfig` en Domain
- O pasar estos valores como parámetros opcionales con defaults
- O usar `ProductSearchParams` con valores por defecto (ya lo tiene)

**Prioridad:** 🟡 **MEDIA** - Afecta flexibilidad y testabilidad

---

### 7. **ApiErrorMapper en Core pero usado en Presentation**

**Ubicación:** `core/utils/ApiErrorMapper.kt`

**Problema:**
- `ApiErrorMapper` está en `core.utils`
- Se usa en `DetailProductViewModel` (Presentation)
- También se usa en Data layer

**Análisis:**
- `ApiErrorMapper` es específico de mapeo de errores de API
- Debería estar en Data layer, no en Core
- Presentation no debería usarlo directamente

**Solución:**
1. Mover `ApiErrorMapper` a `data/mapper/` o `data/utils/`
2. Los UseCases deben retornar errores ya mapeados
3. Presentation solo recibe `ResourceData.Error` con mensaje ya procesado

**Prioridad:** 🟡 **MEDIA** - Mejora organización y separación

---

### 8. **ResourceUiState en Core**

**Ubicación:** `core/utils/ResourceUiState.kt`

**Problema:**
- `ResourceUiState` es específico de Presentation (UI)
- Está en `core.utils` que debería ser compartido

**Análisis:**
- `ResourceUiState` es correcto tenerlo en Core si se comparte
- PERO: `ResourceData` debería estar en Domain
- La separación entre `ResourceData` (Domain) y `ResourceUiState` (Presentation) es correcta

**Solución:**
- Si `ResourceUiState` solo se usa en Presentation, moverlo a `presentation/common/`
- Mantener en Core si se comparte entre módulos

**Prioridad:** 🟢 **BAJA** - Es más una organización que un error

---

### 9. **Falta de UseCases para Auth**

**Problema:**
- No hay UseCases para operaciones de autenticación
- `AuthViewModel` llama directamente a repositorios

**Solución:**
Crear UseCases en `domain/usecase/auth/`:
- `LoginUseCase` - Construir URL de login
- `ExchangeCodeUseCase` - Intercambiar código por token
- `GetAuthStateUseCase` - Obtener estado de autenticación
- `LogoutUseCase` - Cerrar sesión (si existe)

**Prioridad:** 🟡 **MEDIA** - Mejora separación y testabilidad

---

### 10. **GetProductDetailUseCase es un passthrough**

**Ubicación:** `domain/usecase/GetProductDetailUseCase.kt`

**Problema:**
```kotlin
suspend operator fun invoke(id: String) = repo.getProductDetail(id)
```

El UseCase solo delega al repositorio sin lógica adicional.

**Análisis:**
- Esto puede estar bien si no hay lógica de negocio
- PERO: permite agregar validaciones, transformaciones, etc. en el futuro
- El UseCase actúa como punto de entrada único

**Solución:**
- Está bien así, pero se puede mejorar agregando:
  - Validación del ID (no vacío, formato válido)
  - Logging de errores
  - Transformaciones si son necesarias

**Prioridad:** 🟢 **BAJA** - No es un error, pero se puede mejorar

---

## ✅ Aspectos Bien Implementados

1. ✅ **Separación de capas básica**: Domain, Data, Presentation están separados
2. ✅ **Repositorios**: `ProductsRepository` tiene interfaz en Domain e implementación en Data
3. ✅ **Mappers**: Separación entre DTOs (Data) y modelos de dominio
4. ✅ **UseCases para Products**: `SearchProductsPagedUseCase` y `GetProductDetailUseCase` están bien ubicados
5. ✅ **Inyección de dependencias**: Uso correcto de Hilt/Dagger
6. ✅ **Modelos de dominio**: `Product`, `ProductDetail` están en Domain y son independientes

---

## 📐 Diagrama de Dependencias Actual vs Ideal

### ❌ Actual (Violaciones)
```
Presentation
  ↓ (depende de)
Data, Core, Domain ✅
  
Domain
  ↓ (depende de)
Core.utils.ResourceData ❌
Core.storage.TokenStorage ❌
Data.auth.AuthRepository ❌

Data
  ↓ (depende de)
Domain ✅
Core ✅
```

### ✅ Ideal (Clean Architecture)
```
Presentation
  ↓ (solo depende de)
Domain ✅
Core (solo utils compartidos) ✅

Domain
  ↓ (NO depende de nada)
- Solo Kotlin stdlib
- Solo interfaces propias

Data
  ↓ (solo depende de)
Domain ✅
Core (solo config/di) ✅
```

---

## 🎯 Plan de Acción Recomendado

### Fase 1: Correcciones Críticas (ALTA PRIORIDAD)

1. **Mover ResourceData a Domain**
   - Crear `domain/common/ResourceData.kt`
   - Actualizar imports en Domain
   - Actualizar imports en Data y Presentation

2. **Crear interfaz AuthRepository en Domain**
   - Crear `domain/repository/AuthRepository.kt` (interfaz)
   - Renombrar `data/auth/AuthRepository.kt` → `AuthRepositoryImpl.kt`
   - Hacer que implemente la interfaz de Domain

3. **Crear interfaz TokenStorage en Domain**
   - Crear `domain/repository/TokenStorage.kt` (interfaz)
   - Renombrar `core/storage/TokenStorage.kt` → `TokenStorageImpl.kt`
   - Hacer que implemente la interfaz de Domain
   - Actualizar `AuthManager` para usar la interfaz

### Fase 2: Mejoras Importantes (MEDIA PRIORIDAD)

4. **Crear UseCases para Auth**
   - `LoginUseCase`
   - `ExchangeCodeUseCase`
   - `GetAuthStateUseCase`

5. **Refactorizar AuthViewModel**
   - Usar UseCases en lugar de repositorios directos
   - Remover dependencias de Data y Core

6. **Mover ApiErrorMapper a Data**
   - Mover a `data/mapper/` o `data/utils/`
   - Actualizar imports

7. **Mejorar SearchProductsPagedUseCase**
   - Usar configuración o parámetros en lugar de hardcode

### Fase 3: Mejoras Menores (BAJA PRIORIDAD)

8. **Reorganizar ResourceUiState**
   - Evaluar si debe estar en Core o Presentation

9. **Mejorar GetProductDetailUseCase**
   - Agregar validaciones si es necesario

---

## 📝 Resumen de Prioridades

| # | Problema | Prioridad | Impacto |
|---|----------|-----------|---------|
| 1 | Domain depende de Core.utils.ResourceData | 🔴 ALTA | Viola Clean Architecture |
| 2 | Domain depende de Data.auth.AuthRepository | 🔴 ALTA | Viola Clean Architecture |
| 3 | Domain depende de Core.storage.TokenStorage | 🔴 ALTA | Viola Clean Architecture |
| 4 | Presentation depende de Data.auth.AuthRepository | 🟡 MEDIA | Afecta separación |
| 5 | Presentation depende de Core.storage.TokenStorage | 🟡 MEDIA | Afecta separación |
| 6 | UseCase con valores hardcodeados | 🟡 MEDIA | Afecta flexibilidad |
| 7 | ApiErrorMapper en Core | 🟡 MEDIA | Organización |
| 8 | ResourceUiState en Core | 🟢 BAJA | Organización |
| 9 | Falta UseCases para Auth | 🟡 MEDIA | Separación |
| 10 | GetProductDetailUseCase passthrough | 🟢 BAJA | Mejora opcional |

---

## 🔗 Referencias de Clean Architecture

**Principios clave:**
1. **Dependency Rule**: Las dependencias solo apuntan hacia adentro (Domain es el núcleo)
2. **Independence**: Domain no debe depender de frameworks, UI, o Data
3. **Interfaces**: Domain define interfaces, Data implementa
4. **Use Cases**: Contienen lógica de negocio específica de aplicación

**Regla de oro:**
> **Domain NO debe tener dependencias de otras capas. Solo puede depender de la biblioteca estándar de Kotlin/Java.**
