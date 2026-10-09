# Stockify 📦 — Gestión de inventario para tiendas

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=for-the-badge&logo=Firebase&logoColor=white)

![Stockify Logo](Stockify%20Logo.png)

**Stockify** es una app Android nativa (Kotlin + Jetpack Compose + Firebase) para que tiendas y emprendimientos —empezando por indumentaria— administren su **inventario, stock por talles, categorías, movimientos y reportes** en tiempo real y de forma colaborativa.

> 💡 Problema que resuelve: muchos comercios llevan el stock en cuadernos, planillas o WhatsApp, lo que genera pérdidas, errores y desorganización. Stockify centraliza todo en una app moderna y fácil de usar.

---

## ✨ Funcionalidades

| Apartado | Qué incluye |
|----------|-------------|
| 🔐 Auth y roles | Registro/login con Firebase Auth. Negocio multiusuario por **código de tienda**. Roles: **Dueño** (todo), **Administrador** (gestiona), **Empleado** (lectura/ventas). |
| 📊 Dashboard | Bajo stock, agotados, total de categorías, últimos productos y accesos rápidos. |
| 🛍️ Productos | Alta/baja/modificación, buscador en tiempo real, marca, códigos interno/barras, precios costo/venta, colores, foto (ImgBB) y **stock por talle** (XS–XXL). |
| 📁 Categorías | ABM de categorías con subcategorías y vista de productos por categoría. |
| 🔄 Inventario | Entradas/salidas con transacción Firestore (sin stock negativo), historial por producto. |
| 📈 Reportes | Valor del inventario (costo/venta/ganancia potencial), stock por categoría, agotados, top por valor y ganancias diarias. |
| 🤖 Asistente | Recomendaciones de reposición priorizadas (Crítica/Media/Baja) con insights. *Heurística local sobre el stock actual; no es un modelo de IA remoto (ver Roadmap).* |
| 👤 Perfil | Datos del usuario, código de tienda para invitar empleados y gestión de roles (dueño). |
| 🌙 UI | Material 3, **modo oscuro**, transiciones animadas entre pantallas. |

---

## 🛠️ Stack

- **Lenguaje:** Kotlin + Corrutinas/Flow
- **UI:** Jetpack Compose (BOM `2024.04.00`) + Material 3 + Navigation Compose (`2.7.7`)
- **Arquitectura:** MVVM por feature + repositorios (interfaces en `domain`, implementación en `data`)
- **Backend:** Firebase Auth + Firestore (multi-tenant `stores/{storeId}`), reglas en `app_stock/firestore.rules`
- **Imágenes:** subida a [ImgBB](https://api.imgbb.com) (`ImageUploadService` + `BuildConfig`), carga con Coil (`2.6.0`)
- **Build:** `compileSdk/targetSdk 35`, `minSdk 24`, R8/Minify + Shrink en release

---

## 🗂️ Estructura del repo

```text
AppStock/
├── README.md                  # ← este archivo
├── MEJORAS_APLICADAS.md       # auditoría y fixes aplicados
├── DATA_MODEL.md              # modelos + rutas Firestore
├── REQUIREMENTS.md            # requisitos funcionales
├── PRODUCT_VISION.md          # visión del producto
├── DEVELOPMENT_ROADMAP.md     # fases del proyecto
├── UI_GUIDELINES.md           # paleta, Material 3, accesibilidad
├── GEMINI_RULES.md            # reglas del stack para agentes/IA
└── app_stock/                 # proyecto Android (módulo app)
    ├── firestore.rules
    ├── local.properties.ejemplo
    └── app/src/main/kotlin/com/appstock/app_stock/
        ├── domain/            # model/ + repository/ (interfaces) + SessionManager
        ├── data/              # repository/ (impl Firestore) + service/ (ImgBB, IA)
        └── presentation/      # auth, dashboard, products, categories, sizes,
                               # inventory, reports, ai, profile, settings,
                               # navigation, main, ui.theme/components
```

---

## 🔥 Datos (resumen)

Multi-tenant: todo cuelga de `stores/{storeId}`.

```text
stores/{storeId}/products/{productId}              # ProductDetail
stores/{storeId}/products/{productId}/sizes/{id}   # ProductSize (doc id = size.id)
stores/{storeId}/categories/{categoryId}           # Category (+ subcategories)
stores/{storeId}/inventory_history/{movementId}    # InventoryMovement
users/{uid}                                        # perfil (storeId + role)
stores/{storeId}                                   # doc base de la tienda
```

Detalle de campos en [`DATA_MODEL.md`](DATA_MODEL.md).

---

## 🚀 Instalación (desarrolladores)

1. **Clonar**
   ```bash
   git clone https://github.com/MaxAxS1/AppStock.git
   cd AppStock/app_stock
   ```
   El código Android vive en `app_stock/app/` (package `com.appstock.app_stock`).

2. **Firebase**
   - Crear proyecto en [Firebase Console](https://console.firebase.google.com/), registrar la app (`com.appstock.app_stock`).
   - Descargar `google-services.json` → colocarlo en `app_stock/app/google-services.json`.
   - Habilitar **Authentication (Email/Password)** y **Firestore**.
   - ⚠️ `google-services.json` **no se commitea** (está en `.gitignore`; cada entorno lo provisiona).

3. **Key de ImgBB** (fotos de productos; no se usa Firebase Storage)
   - Crear cuenta en [api.imgbb.com](https://api.imgbb.com) y obtener la API key.
   - Agregarla en `app_stock/local.properties` (no commiteado, ver `local.properties.ejemplo`):
     ```properties
     imgbb.api.key=TU_API_KEY_AQUI
     ```
   - Se expone como `BuildConfig.IMGBB_API_KEY` desde `app/build.gradle.kts`. Nunca hardcodear keys en `.kt`.

4. **Reglas de Firestore**
   ```bash
   firebase deploy --only firestore:rules
   ```
   Las reglas (`app_stock/firestore.rules`) aíslan por tienda según `users/{uid}.storeCode`. No usar modo test abierto en producción.

5. **Correr**
   - Abrir `app_stock/` en Android Studio, sincronizar Gradle y ejecutar en emulador/dispositivo.

---

## 🗺️ Roadmap y estado

| Fase | Alcance | Estado |
|------|---------|--------|
| 1–2 | Base Firebase, arquitectura, tema, auth | ✅ Hecho |
| 3–6 | Dashboard, categorías, productos, talles | ✅ Hecho |
| 7–8 | Inventario, reportes | ✅ Hecho (inventario sin ruta en Nav: pendiente enlazar) |
| 9 | IA | ⚠️ Parcial: heurística local + mock; falta historial real / modelo remoto |
| — | Ajustes (exportar/sync/pass) | ⚠️ Simulado con snackbar; pendiente implementar o marcar “Próximamente” |

Historial de mejoras en [`MEJORAS_APLICADAS.md`](MEJORAS_APLICADAS.md).

---

## 🤝 Contribuir

1. Fork del repo.
2. Rama `feature/NombreFuncionalidad`.
3. Commit + push + Pull Request contra `inicial`.

Reglas del stack en [`GEMINI_RULES.md`](GEMINI_RULES.md) y guía visual en [`UI_GUIDELINES.md`](UI_GUIDELINES.md).

---

## 📄 Licencia

Previsto **MIT** (pendiente agregar archivo `LICENSE`).

> Desarrollado con ❤️ para pequeños y medianos emprendimientos.
