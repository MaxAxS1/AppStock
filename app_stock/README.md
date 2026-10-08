# Stockify (AppStock) 📦

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=for-the-badge&logo=Firebase&logoColor=white)

**Stockify** es una aplicación móvil nativa para Android, desarrollada con Kotlin y Jetpack Compose, enfocada en la **gestión moderna de inventario y stock** para tiendas (como tiendas de indumentaria y pequeños negocios).

Ofrece una interfaz amigable, vibrante y atractiva, con un fuerte enfoque en el trabajo colaborativo en tiempo real a través de la integración nativa con Firebase.

---

## ✨ Características Principales

*   🔐 **Autenticación y Sistema Multi-Rol**
    *   Registro e inicio de sesión seguros mediante Firebase Authentication.
    *   Gestión de negocios multiusuario mediante un código de tienda único.
    *   Soporte para 3 niveles de roles: **Dueño** (control total), **Administrador** (gestión de stock) y **Empleado** (solo lectura/ventas).
    *   Opción para habilitar/deshabilitar usuarios desde el panel del dueño.

*   📊 **Dashboard Analítico**
    *   Vista general inmediata del estado de tu negocio.
    *   Indicadores clave como cantidad de productos con bajo stock, productos agotados y total de categorías.
    *   Acceso rápido a funcionalidades principales y resumen visual dinámico de últimos productos.

*   🛍️ **Gestión de Inventario Completa**
    *   Listado general y buscador en tiempo real de artículos.
    *   Detalles exhaustivos de producto: Nombre, Descripción, Marca, Código de Barras, Código Interno, Precios de Compra/Venta.
    *   Soporte avanzado de atributos: **Talles con stock dinámico** y **Variantes de colores**.
    *   Posibilidad de asignar fotografías a los productos (vía URI de la galería).

*   📁 **Categorías y Organización**
    *   Creación, edición y eliminación de categorías (con soporte futuro para subcategorías).
    *   Filtro y estructuración lógica del inventario.

*   📈 **Reportes Financieros**
    *   Tres vistas de reportes: **Resumen**, **Más vendidos** y **Ganancias**.
    *   Cálculo automático de ganancia potencial, stock valorizado a costo y precio de venta final.
    *   Gráficos visuales dinámicos del top de inventario.

*   🤖 **Asistente Inteligente (AI)**
    *   Análisis proactivo y automático del inventario.
    *   Emite recomendaciones priorizadas (Alta, Media, Baja) para alertar sobre niveles críticos de stock y tendencias de reposición.

---

## 🛠️ Stack Tecnológico

El proyecto se rige por las mejores prácticas actuales de Android Development:

*   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI Declarativa.
*   **Lenguaje**: [Kotlin](https://kotlinlang.org/) (Soporte completo de Corrutinas y Flow para asincronía).
*   **Arquitectura**: MVVM (Model-View-ViewModel) + Repositorios.
*   **Backend / DB**: [Firebase Firestore](https://firebase.google.com/docs/firestore) para base de datos NoSQL en tiempo real y sincronización en la nube.
*   **Autenticación**: [Firebase Auth](https://firebase.google.com/docs/auth).
*   **Navegación**: `androidx.navigation:navigation-compose`.
*   **Imágenes**: [Coil Compose](https://coil-kt.github.io/coil/compose/) para la carga asíncrona de imágenes en caché.
*   **Diseño**: Implementación de Material Design 3 con un theme personalizado enfocado en colores cálidos (Naranjas y Blancos).

---

## 🚀 Arquitectura del Proyecto

El código fuente está estructurado siguiendo un patrón de separación de responsabilidades:

```text
com.appstock.app_stock
│
├── domain/            # Reglas de negocio y entidades
│   ├── model/         # Clases de datos (User, Product, Category, etc.)
│   └── repository/    # Interfaces de acceso a datos y SessionManager
│
├── data/              # Implementación de persistencia y red
│   └── repository/    # Repositorios concretos conectados a Firebase (AuthRepositoryImpl, ProductRepositoryImpl)
│
└── presentation/      # Capa visual (Compose)
    ├── ui.theme/      # Colores, tipografías y formas (Custom Branding)
    ├── navigation/    # Rutas y grafos de navegación
    ├── auth/          # Login, Register
    ├── dashboard/     # Pantalla de inicio y métricas rápidas
    ├── products/      # ABM de productos, formulario de carga, listado
    ├── categories/    # Pantallas de gestión de categorías
    ├── reports/       # Análisis estadístico y de ganancias
    ├── profile/       # Gestión de cuenta, perfil y permisos de empleados
    └── ai/            # Pantalla del asistente inteligente de análisis
```

---

## 📱 Instalación y Configuración (Para Desarrolladores)

1.  **Clonar el Repositorio**
    ```bash
    git clone https://github.com/tu-usuario/app-stockify.git
    cd app-stockify/app_stock/app/
    ```
    > El código Android vive en `app_stock/app/` (módulo `app`, package `com.appstock.app_stock`).

2.  **Configurar Firebase**
    *   Este proyecto requiere estar conectado a Firebase.
    *   Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
    *   Registra tu aplicación Android (con el package `com.appstock.app_stock`).
    *   Descarga el archivo `google-services.json` proporcionado por Firebase.
    *   Colócalo dentro del directorio `app/` (`app_stock/app/google-services.json`).
    *   ⚠️ **No commitear `google-services.json`**: es un secreto por entorno. Si ya fue commiteado, quitarlo del índice con `git rm --cached app_stock/app/google-services.json` y añadir la ruta al `.gitignore`. Cada desarrollador/CI lo provisiona localmente.
    *   Habilita **Authentication** (Email/Password) y **Firestore Database** en la consola.

2b. **Configurar key de ImgBB (subida de imágenes)**
    *   Las imágenes de producto se suben a [ImgBB](https://api.imgbb.com) vía `data/service/ImageUploadService.kt` (Firebase Storage no se usa: sin plan Blaze).
    *   Crea una cuenta gratuita en ImgBB y obtén tu API key.
    *   Añádela en `app_stock/local.properties` (archivo local, no commiteado):
        ```properties
        imgbb.api.key=TU_API_KEY_AQUI
        ```
    *   Expón la key vía `BuildConfig` en `app/build.gradle.kts` (leer `local.properties` + `buildConfigField("String", "IMGBB_API_KEY", ...)`), y consúmela como `BuildConfig.IMGBB_API_KEY`.
    *   ⚠️ **Nunca hardcodees la key en un `.kt`.** Estado actual: `ImageUploadService.kt:28` aún contiene una key hardcodeada → rota la key en ImgBB y migra a `BuildConfig` (ver `MEJORAS_APLICADAS.md`).

2c. **Reglas de Firestore**
    *   Este repo aún no incluye `firestore.rules`. Publica reglas que aíslen por tienda (`stores/{storeId}` + Auth con custom claims o validación de membresía) y despliega con:
        ```bash
        firebase deploy --only firestore:rules
        ```
    *   No publiques el proyecto con reglas abiertas en producción (ver `MEJORAS_APLICADAS.md`).

> **Versiones verificadas en `app/build.gradle.kts`:** `compileSdk = 35`, `targetSdk = 35`, `minSdk = 24`.

3.  **Compilar el Proyecto**
    *   Abre el proyecto usando **Android Studio** (Koala o superior recomendado).
    *   Espera a que Gradle sincronice las dependencias de Compose y Firebase.
    *   Ejecuta el proyecto en un Emulador o Dispositivo Físico.

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Si deseas mejorar Stockify:

1. Haz un *Fork* del proyecto.
2. Crea una nueva rama para tu funcionalidad (`git checkout -b feature/NuevaFuncionalidad`).
3. Haz commit de tus cambios (`git commit -m 'Añadir nueva funcionalidad'`).
4. Haz push a la rama (`git push origin feature/NuevaFuncionalidad`).
5. Abre un **Pull Request**.

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia **MIT**. Lee el archivo `LICENSE` (si está disponible) para obtener más detalles.

---

> Desarrollado con ❤️ para mejorar la administración de pequeños y medianos emprendimientos.
