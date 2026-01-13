# 🧪 Guía de Pruebas - Validación de Conexión a Internet

## 🎯 Objetivo

Esta guía te ayudará a verificar que la validación de conexión a internet funciona correctamente en tu app.

---

## 📱 Pruebas Manuales en Dispositivo/Emulador

### Preparación

1. **Compilar la app**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Instalar en dispositivo**
   ```bash
   ./gradlew installDebug
   ```

3. **Abrir Logcat** para ver los logs:
   ```bash
   adb logcat -s NETWORK_MONITOR HOME_VM DETAIL_VM
   ```

---

### Test 1: Estado Inicial con Conexión

**Pasos:**
1. Asegúrate de tener WiFi o datos móviles activos
2. Abre la app

**Resultado Esperado:**
- ✅ La app se abre normalmente
- ✅ En Logcat: `🔌 NetworkMonitor iniciado`
- ✅ En Logcat: `📊 Estado inicial de conexión: true`
- ✅ No aparece la vista de "Sin conexión"

---

### Test 2: Perder Conexión en Home Screen

**Pasos:**
1. Abre la app
2. Estando en la pantalla de Home
3. **Activa modo avión** o desactiva WiFi/datos

**Resultado Esperado:**
- ✅ Aparece inmediatamente `NoInternetView`
- ✅ Muestra ilustración de sin conexión
- ✅ Muestra mensaje: "Sin conexión a internet"
- ✅ Muestra botón "Reintentar"
- ✅ En Logcat: `❌ Red perdida: [...]`
- ✅ En Logcat: `⚠️ Sin conexión a internet`

---

### Test 3: Recuperar Conexión

**Pasos:**
1. Con la vista de "Sin conexión" visible
2. **Desactiva modo avión** o activa WiFi/datos

**Resultado Esperado:**
- ✅ La vista de "Sin conexión" desaparece automáticamente
- ✅ Vuelve a la vista normal de Home
- ✅ En Logcat: `✅ Red disponible: [...]`

---

### Test 4: Buscar sin Conexión

**Pasos:**
1. Activa modo avión
2. Abre la app
3. Intenta escribir en la barra de búsqueda
4. Presiona buscar

**Resultado Esperado:**
- ✅ La vista de "Sin conexión" permanece visible
- ✅ No se ejecuta la búsqueda
- ✅ El input está visible pero no hace peticiones

---

### Test 5: Botón Reintentar

**Pasos:**
1. Con modo avión activo, abre la app
2. Aparece "Sin conexión"
3. Desactiva modo avión
4. Presiona el botón "Reintentar"

**Resultado Esperado:**
- ✅ La vista de "Sin conexión" desaparece
- ✅ Si había una búsqueda previa, se reintenta
- ✅ La app funciona normalmente

---

### Test 6: Detalle de Producto sin Conexión

**Pasos:**
1. Con conexión activa, busca un producto
2. Haz clic en un producto para ver el detalle
3. Espera a que cargue
4. Regresa a Home
5. Activa modo avión
6. Intenta ver el detalle de otro producto

**Resultado Esperado:**
- ✅ Aparece `NoInternetView` en la pantalla de detalle
- ✅ Muestra el botón "Reintentar"
- ✅ En Logcat: `⚠️ Sin conexión a internet - No se puede cargar el detalle`

---

### Test 7: Transición WiFi → Datos Móviles

**Pasos:**
1. Conecta a WiFi
2. Abre la app (debe funcionar normal)
3. Desactiva WiFi pero mantén datos móviles activos

**Resultado Esperado:**
- ✅ NO debe aparecer "Sin conexión"
- ✅ La app cambia automáticamente a datos móviles
- ✅ Continúa funcionando sin interrupciones
- ✅ En Logcat: `🔄 Capacidades de red cambiadas`

---

### Test 8: WiFi Conectado pero sin Internet

**Pasos:**
1. Conecta a una red WiFi sin acceso a internet
   - Puedes crear un hotspot móvil sin datos
   - O conectar a un router sin internet
2. Abre la app

**Resultado Esperado:**
- ✅ Detecta que no hay internet real
- ✅ Muestra `NoInternetView`
- ✅ En Logcat: `⚠️ Sin conexión a internet`
- ✅ Esto demuestra que `NET_CAPABILITY_VALIDATED` funciona

---

### Test 9: Rotación de Pantalla

**Pasos:**
1. Con conexión activa, abre la app
2. Busca algo
3. Activa modo avión → aparece "Sin conexión"
4. **Rota la pantalla** (portrait ↔ landscape)

**Resultado Esperado:**
- ✅ El estado de "Sin conexión" se mantiene
- ✅ No se pierde el estado tras rotación
- ✅ No hay flickering o recomposiciones innecesarias

---

### Test 10: Background → Foreground

**Pasos:**
1. Con conexión activa, abre la app
2. Minimiza la app (presiona Home)
3. Activa modo avión
4. Vuelve a la app

**Resultado Esperado:**
- ✅ Al volver, muestra inmediatamente "Sin conexión"
- ✅ El NetworkMonitor detectó el cambio en background

---

## 🧪 Pruebas Unitarias

### Ejecutar Tests de NetworkMonitor

```bash
./gradlew test --tests "HomeViewModelNetworkTest"
```

**Tests incluidos:**
- ✅ `initial state is connected`
- ✅ `when network is lost, isConnected emits false`
- ✅ `when network is restored, isConnected emits true`
- ✅ `network state changes are reflected in viewModel`

**Resultado Esperado:**
```
HomeViewModelNetworkTest > initial state is connected PASSED
HomeViewModelNetworkTest > when network is lost, isConnected emits false PASSED
HomeViewModelNetworkTest > when network is restored, isConnected emits true PASSED
HomeViewModelNetworkTest > network state changes are reflected in viewModel PASSED

BUILD SUCCESSFUL
```

---

## 📊 Comandos de Debug

### Ver logs en tiempo real

```bash
# Solo logs de red
adb logcat -s NETWORK_MONITOR:V

# Logs de ViewModels y red
adb logcat -s NETWORK_MONITOR:V HOME_VM:V DETAIL_VM:V

# Todos los logs relevantes
adb logcat | grep -E "(NETWORK_MONITOR|HOME_VM|DETAIL_VM|NoInternet)"
```

### Forzar cambios de red en emulador

En la barra lateral del emulador:
1. Click en "..." (Extended controls)
2. Seleccionar "Cellular"
3. Cambiar "Network type" a "None" (simula sin red)
4. Cambiar de vuelta a "LTE" (simula recuperación)

### Forzar cambios de red con ADB

```bash
# Simular sin conexión (solo emulador)
adb shell svc wifi disable
adb shell svc data disable

# Restaurar conexión
adb shell svc wifi enable
adb shell svc data enable
```

---

## ✅ Checklist de Validación

Marca cada prueba al completarla:

### Home Screen
- [ ] Estado inicial con conexión
- [ ] Detecta pérdida de conexión
- [ ] Detecta recuperación de conexión
- [ ] Muestra NoInternetView correctamente
- [ ] Botón Reintentar funciona

### Detail Screen
- [ ] Detecta sin conexión al intentar cargar
- [ ] Muestra NoInternetView
- [ ] Botón Reintentar funciona

### Transiciones
- [ ] WiFi → Datos móviles (sin mostrar error)
- [ ] Datos → WiFi (sin mostrar error)
- [ ] WiFi sin internet → muestra error

### Estabilidad
- [ ] Rotación de pantalla mantiene estado
- [ ] Background → Foreground funciona
- [ ] No hay memory leaks (usar LeakCanary)
- [ ] No hay crashes relacionados con red

### Tests Unitarios
- [ ] Todos los tests pasan
- [ ] FakeNetworkMonitor funciona correctamente

---

## 🐛 Problemas Comunes y Soluciones

### Problema: La app no detecta cambios de red

**Posibles causas:**
- Permisos no otorgados
- NetworkCallback no registrado

**Solución:**
```kotlin
// Verificar en AndroidManifest.xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

// Verificar logs
adb logcat -s NETWORK_MONITOR:V
```

---

### Problema: NoInternetView no se muestra

**Posibles causas:**
- `isConnected` no se está observando en la UI
- Estado inicial optimista oculta el error

**Solución:**
```kotlin
// Verificar que HomeScreen tiene el parámetro
@Composable
fun HomeScreen(
    isConnected: Boolean,  // ✅ Debe estar presente
    // ...
)

// Verificar que se observa en HomeRoute
val isConnected by viewModel.isConnected.collectAsState()  // ✅
```

---

### Problema: La app muestra "Sin conexión" aunque hay WiFi

**Causas:**
- WiFi conectado pero sin internet real
- Router sin conexión a internet

**Solución:**
- Esto es el comportamiento **correcto**
- `NET_CAPABILITY_VALIDATED` verifica internet real
- Conecta a una red con internet funcional

---

### Problema: Logs no aparecen

**Solución:**
```bash
# Verificar que el dispositivo está conectado
adb devices

# Limpiar logs y empezar de nuevo
adb logcat -c
adb logcat -s NETWORK_MONITOR:V HOME_VM:V
```

---

## 📈 Métricas de Éxito

### Funcionalidad
- ✅ 100% de los tests manuales pasan
- ✅ 100% de los tests unitarios pasan
- ✅ No crashes relacionados con red

### Performance
- ✅ Detección de cambios < 1 segundo
- ✅ Sin lags en la UI al cambiar estado
- ✅ Batería no afectada significativamente

### UX
- ✅ Mensaje claro y amigable
- ✅ Ilustración visible
- ✅ Botón de reintentar funcional
- ✅ Transición suave entre estados

---

## 🎓 Aprendizajes Clave

1. **NetworkCallback es más confiable** que BroadcastReceiver deprecated
2. **NET_CAPABILITY_VALIDATED es crucial** para detectar internet real
3. **callbackFlow gestiona el lifecycle** automáticamente
4. **StateFlow en ViewModel** permite observación reactiva en Compose
5. **Clean Architecture facilita testing** y mantenimiento

---

## 📚 Recursos Adicionales

- [NetworkCallback Documentation](https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback)
- [Monitoring Network State](https://developer.android.com/training/monitoring-device-state/connectivity-status-type)
- [Testing Kotlin Flows](https://developer.android.com/kotlin/flow/test)

---

**¡Pruebas completadas!** 🎉

Si todos los tests pasan, la implementación está lista para producción.
