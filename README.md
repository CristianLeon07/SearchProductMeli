## Product Search App
Aplicación Android desarrollada como prueba técnica para Mercado Libre, orientada a demostrar buenas prácticas de desarrollo, 
esta app permite realizar una autenticación directa con MercadoLibre, buscar productos, visualizar resultados paginados, consultar
el detalle de cada producto y manejar escenarios sin conexión a internet, manteniendo el estado ante rotaciones de pantalla.

## Funcionalidades
•	 Búsqueda de productos por texto
•	 Listado de resultados con imágenes
•	 Detalle del producto con carrusel de imágenes
•	 Detección de conectividad y manejo de errores de red
•	 Persistencia de estado al rotar el dispositivo
•	 Reintento automático al recuperar conexión

 ## Características Técnicas
•	Paginación infinita con Paging 3
•	Caché inteligente
•	Autenticación OAuth con Mercado Libre
•	Manejo de errores con mensajes claros en español
•	Tests unitarios enfocados en lógica de negocio
•	UI declarativa moderna con Jetpack Compose

 ## Stack Tecnológico
•	Lenguaje: Kotlin
•	UI: Jetpack Compose
•	Arquitectura: MVVM + Clean Architecture
•	Async: Coroutines + Flow
•	Networking: Retrofit + OkHttp
•	Paginación: Paging 3
•	Carga de imágenes: Coil
•	Inyección de dependencias: Hilt
•	Persistencia: DataStore
•	Testing: MockK


## Ejecución del Proyecto
Requisitos
•	Android Studio
•	Dispositivo o emulador con Android

## Pasos
git clone https://github.com/CristianLeon07/PruebaTecnicaMeli.git
1.	Abre el proyecto en Android Studio
2.	Espera la sincronización de Gradle
3.	Conecta un dispositivo o inicia un emulador
4.	Ejecuta la app

## Uso de la Aplicación
1.	Te autenticas con la cuenta registrada en Meli
2.	Ingresa un término de búsqueda (ej: televisor)
3.	Visualiza los resultados paginados
4.	Selecciona un producto para ver su detalle
5.	Navega el carrusel de imágenes
6.	Si no hay internet, la app muestra un estado visual y permite reintentar


ANEXO.

## Video Demostración

Video demostrativo de la aplicación:

## 🎥 Demo en video

[![Ver demo](https://img.youtube.com/vi/ycLAbj9KygM/hqdefault.jpg)](https://youtube.com/shorts/ycLAbj9KygM)
