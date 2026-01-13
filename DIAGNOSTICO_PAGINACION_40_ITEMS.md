# 🔍 Diagnóstico: Paginación se detiene en 40 productos

## 🎯 Problema Identificado

Tu paginación se detiene en **40 productos** porque:

### **Causa Principal: Limitación del API de MercadoLibre** 🔴

El endpoint que estás usando:
```
GET https://api.mercadolibre.com/products/search
```

Este es el **endpoint de catálogo de productos**, que tiene **limitaciones diferentes** al endpoint de búsqueda principal.

---

## 📊 Análisis de tu Configuración Actual

### **Tu PagingConfig:**
```kotlin
PagingConfig(
    pageSize = 20,              // ✅ 20 items por página
    initialLoadSize = 40,       // ✅ Primera carga: 40 items (2 páginas)
    prefetchDistance = 20,      // ✅ Prefetch correcto
    enablePlaceholders = false
)
```

### **Flujo de carga:**
```
1. Primera carga: offset=0, limit=40  → API retorna 40 productos ✅
2. Segunda carga: offset=40, limit=20 → API retorna 0 productos ❌
   └─ nextKey se vuelve null
   └─ endOfPaginationReached = true
```

---

## 🔍 Posibles Causas

### **1. API está limitando resultados** 🔴 PROBABLE

El endpoint `/products/search` puede tener límites:
- Máximo 40 items por query
- Máximo offset permitido
- Restricciones en la API pública

**Cómo verificar:**
```kotlin
// En ProductsPagingSource.load()
Log.d("PAGING", "Requesting: offset=$offset, limit=$limit")
Log.d("PAGING", "API returned: ${products.size} products, total=$total")
Log.d("PAGING", "NextKey: $nextKey")
```

**Si ves:**
```
Requesting: offset=0, limit=40
API returned: 40 products, total=1000  ✅
NextKey: 40

Requesting: offset=40, limit=20
API returned: 0 products, total=1000   ❌ PROBLEMA
NextKey: null
```

Entonces el API está limitando las respuestas.

---

### **2. Endpoint incorrecto** 🟡 POSIBLE

Estás usando: `GET /products/search`

**Deberías usar:** `GET /sites/{SITE_ID}/search` (API principal)

```kotlin
// Endpoint correcto de MercadoLibre
@GET("sites/{site_id}/search")
suspend fun searchProducts(
    @Path("site_id") siteId: String,  // ✅ "MCO" para Colombia
    @Query("q") query: String,
    @Query("offset") offset: Int,
    @Query("limit") limit: Int,
    @Query("status") status: String? = null
): Response<ProductsSearchResponseDto>
```

**URL correcta:**
```
https://api.mercadolibre.com/sites/MCO/search?q=laptop&offset=0&limit=20
```

---

### **3. Parámetros incorrectos en el request** 🟢 POCO PROBABLE

Tu código está correcto:
```kotlin
val response = api.searchProducts(
    query = searchParams.query,
    domainId = searchParams.domainId,
    siteId = searchParams.siteId,    // "MCO"
    status = searchParams.status,
    offset = offset,                 // ✅ Incrementa correctamente
    limit = limit                    // ✅ 20 o 40
)
```

---

## 🛠️ Soluciones

### **Solución 1: Usar el endpoint correcto de MercadoLibre** ✅ RECOMENDADO

#### **Paso 1: Actualizar la interfaz del API**

```kotlin
// SearchProductsApi.kt
interface SearchProductsApi {

    // ✅ Endpoint correcto para búsquedas
    @GET("sites/{site_id}/search")
    suspend fun searchProducts(
        @Path("site_id") siteId: String,
        @Query("q") query: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null
    ): Response<ProductsSearchResponseDto>

    @GET("items/{id}")  // ✅ También cambiar este si es necesario
    suspend fun getProductDetail(
        @Path("id") id: String
    ): Response<ProductDetailDto>
}
```

#### **Paso 2: Actualizar ProductsPagingSource**

```kotlin
// ProductsPagingSource.kt
override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
    return try {
        val offset = params.key ?: searchParams.offset
        val limit = params.loadSize
        
        val response = api.searchProducts(
            siteId = searchParams.siteId,     // ✅ Ahora es Path parameter
            query = searchParams.query,
            offset = offset,
            limit = limit,
            status = searchParams.status
            // domainId ya no se usa en este endpoint
        )
        
        // ... resto del código igual
    } catch (e: Exception) {
        LoadResult.Error(e)
    }
}
```

#### **Paso 3: Actualizar ProductsRepositoryImpl**

```kotlin
// ProductsRepositoryImpl.kt
override fun searchProductsPaged(
    params: ProductSearchParams
): Flow<PagingData<Product>> {
    
    val pageSize = params.limit.coerceIn(
        AppConfig.Search.MIN_PAGE_SIZE,
        AppConfig.Search.MAX_PAGE_SIZE
    )
    
    return Pager(
        config = PagingConfig(
            pageSize = pageSize,
            initialLoadSize = pageSize * 2,
            prefetchDistance = pageSize,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            ProductsPagingSource(
                api = api,
                searchParams = params.copy(limit = pageSize)
            )
        }
    ).flow
}
```

---

### **Solución 2: Agregar logs para diagnosticar** 🔍 TEMPORAL

Si quieres confirmar qué está pasando:

```kotlin
// ProductsPagingSource.kt
override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
    return try {
        val offset = params.key ?: searchParams.offset
        val limit = params.loadSize
        
        Log.d("PAGING_DEBUG", "========== NUEVA CARGA ==========")
        Log.d("PAGING_DEBUG", "Offset: $offset")
        Log.d("PAGING_DEBUG", "Limit: $limit")
        Log.d("PAGING_DEBUG", "Query: ${searchParams.query}")
        
        val response = api.searchProducts(...)
        
        if (!response.isSuccessful) {
            Log.e("PAGING_DEBUG", "❌ HTTP Error: ${response.code()}")
            return LoadResult.Error(retrofit2.HttpException(response))
        }
        
        val body = response.body()
            ?: return LoadResult.Error(IllegalStateException("Body nulo"))
        
        val products = body.results.map { it.toDomain() }
        val total = body.paging.total
        
        Log.d("PAGING_DEBUG", "✅ Productos recibidos: ${products.size}")
        Log.d("PAGING_DEBUG", "Total en API: $total")
        Log.d("PAGING_DEBUG", "Offset actual: ${body.paging.offset}")
        Log.d("PAGING_DEBUG", "Limit actual: ${body.paging.limit}")
        
        val prevKey = if (offset == 0) null else maxOf(0, offset - limit)
        val nextOffset = offset + products.size
        val nextKey = if (products.isEmpty() || nextOffset >= total) null else nextOffset
        
        Log.d("PAGING_DEBUG", "PrevKey: $prevKey")
        Log.d("PAGING_DEBUG", "NextKey: $nextKey")
        Log.d("PAGING_DEBUG", "NextOffset: $nextOffset")
        Log.d("PAGING_DEBUG", "¿Fin de paginación? ${nextKey == null}")
        
        LoadResult.Page(
            data = products,
            prevKey = prevKey,
            nextKey = nextKey
        )
        
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("PAGING_DEBUG", "❌ Exception: ${e.message}", e)
        LoadResult.Error(e)
    }
}
```

**Ejecuta tu app y observa los logs:**

Si ves:
```
========== NUEVA CARGA ==========
Offset: 0
Limit: 40
Query: laptop
✅ Productos recibidos: 40
Total en API: 1000
NextKey: 40

========== NUEVA CARGA ==========
Offset: 40
Limit: 20
Query: laptop
✅ Productos recibidos: 0    ← ❌ PROBLEMA
Total en API: 1000
NextKey: null
```

Entonces confirmas que el API está limitando.

---

### **Solución 3: Ajustar límites del API** 🟡 WORKAROUND

Si el API solo permite 40 items máximo:

```kotlin
// AppConfig.kt
object Search {
    const val DEFAULT_SITE_ID = "MCO"
    const val DEFAULT_STATUS = "active"
    const val PAGE_SIZE = 20
    const val MIN_PAGE_SIZE = 10
    const val MAX_PAGE_SIZE = 40  // ✅ Reducir a 40 si es el límite
    const val MAX_INITIAL_LOAD = 40  // ✅ Agregar límite
}

// ProductsRepositoryImpl.kt
PagingConfig(
    pageSize = minOf(pageSize, 20),  // ✅ Máximo 20 por página
    initialLoadSize = minOf(pageSize * 2, AppConfig.Search.MAX_INITIAL_LOAD),
    prefetchDistance = pageSize,
    enablePlaceholders = false
)
```

---

## 🎯 Documentación de MercadoLibre

### **Endpoints Correctos:**

#### **1. Búsqueda de items (RECOMENDADO)**
```
GET https://api.mercadolibre.com/sites/{SITE_ID}/search
```

**Parámetros:**
- `q` (required): Query de búsqueda
- `offset`: Desde qué item empezar (default: 0)
- `limit`: Cuántos items retornar (default: 50, max: 50)
- `status`: Estado de los items (active, paused, etc.)

**Ejemplo:**
```
https://api.mercadolibre.com/sites/MCO/search?q=laptop&offset=0&limit=20
```

**Límites:**
- ✅ Máximo 50 items por request
- ✅ Puede paginar hasta miles de resultados
- ✅ Offset máximo: depende del total de resultados

#### **2. Catálogo de productos (TU ACTUAL)**
```
GET https://api.mercadolibre.com/products/search
```

**Limitaciones conocidas:**
- ⚠️ API más restrictiva
- ⚠️ Puede limitar offset máximo
- ⚠️ Puede limitar total de resultados

---

## ✅ Solución Recomendada

### **Implementar el cambio al endpoint correcto:**

**Archivo:** `SearchProductsApi.kt`
```kotlin
interface SearchProductsApi {

    @GET("sites/{site_id}/search")
    suspend fun searchProducts(
        @Path("site_id") siteId: String,
        @Query("q") query: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("status") status: String? = null
    ): Response<ProductsSearchResponseDto>

    @GET("items/{id}")
    suspend fun getProductDetail(
        @Path("id") id: String
    ): Response<ProductDetailDto>
}
```

**Beneficios:**
- ✅ Paginación completa (hasta 1000+ productos)
- ✅ Límite de 50 items por request
- ✅ API más estable y documentada
- ✅ Mejor performance

---

## 🧪 Prueba Después del Cambio

1. **Limpia el proyecto:**
   ```bash
   ./gradlew clean
   ```

2. **Ejecuta la app y busca "laptop"**

3. **Scrollea hacia abajo**

4. **Verifica en Logcat:**
   ```
   PAGING_DEBUG: Productos recibidos: 40  (primera carga)
   PAGING_DEBUG: Productos recibidos: 20  (segunda carga) ✅
   PAGING_DEBUG: Productos recibidos: 20  (tercera carga) ✅
   ```

---

## 📝 Resumen

| Problema | Causa | Solución |
|----------|-------|----------|
| Solo 40 productos | Endpoint incorrecto o API limitada | Usar `/sites/{SITE_ID}/search` |
| Paginación se detiene | nextKey se vuelve null | Cambiar endpoint y verificar logs |
| Límite de 40 items | API `/products/search` restrictiva | Migrar a API principal |

**Acción requerida:** Cambiar al endpoint correcto de MercadoLibre.

**Tiempo estimado:** 15-30 minutos de cambios + testing.
