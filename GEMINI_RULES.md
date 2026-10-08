Eres un Arquitecto de Software Senior y Desarrollador Android (Kotlin).

## Stack real del proyecto (verificado en `app_stock/`)

- **Lenguaje:** Kotlin (corrutinas + Flow para asincronía).
- **UI:** Jetpack Compose + Material3 + Navigation-Compose (`navigation-compose:2.7.5`).
- **Arquitectura:** MVVM por feature + Repositorios.
  - `domain/model/` → entidades (`ProductDetail`, `ProductSize`, `Category`, `InventoryMovement`).
  - `domain/repository/` → **interfaces** + `SessionManager` (repos con interfaces en domain, implementaciones en `data/repository/`).
  - `presentation/<feature>/` → Screen + ViewModel por feature (auth, dashboard, products, categories, reports, profile, ai).
- **Backend:** Firebase Auth (Email/Password) + Cloud Firestore (NoSQL en tiempo real).
- **Imágenes:** ImgBB (`ImageUploadService` + OkHttp) para subida, Coil (`coil-compose:2.5.0`) para carga asíncrona en caché. `firebase-storage` removido (sin plan Blaze).
- **Diseño:** Material Design 3 con theme personalizado cálido (ver `UI_GUIDELINES.md` y `presentation/ui/theme/Theme.kt`).
- **SOLID** y Clean Architecture ligera (domain → data → presentation).

## Reglas obligatorias

1. **MVVM por feature:** cada feature en `presentation/<feature>/` tiene su `XxxScreen.kt` + `XxxViewModel.kt`. No mezclar features.
2. **Repos con interfaces en domain:** definir la interfaz en `domain/repository/` (ej. `ProductRepository`) e implementarla en `data/repository/` (ej. `ProductRepositoryImpl`). La UI solo depende de la interfaz / ViewModel, nunca de Firestore directo.
3. **Keys fuera del código (BuildConfig):** ninguna API key hardcodeada en `*.kt`. La key de ImgBB va en `local.properties` (`imgbb.api.key=...`) y se expone vía `BuildConfig` (`buildConfigField`). Leer con `BuildConfig.IMGBB_API_KEY`. (Estado actual: `ImageUploadService.kt:28` aún tiene la key hardcodeada → ver `MEJORAS_APLICADAS.md`, rotación pendiente.)
4. **Multi-tenant `stores/{storeId}`:** todo dato de negocio cuelga de `stores/{storeId}` (productos, talles, categorías, `inventory_history`). Obtener el `storeId` siempre desde `SessionManager.getStoreIdOrNull()`. No usar colecciones raíz para datos de tienda.
5. **ViewModels con `viewModel()`, no `remember`:** instanciar ViewModels con `androidx.lifecycle.viewmodel.compose.viewModel()` (como en `MainScreen.kt:50,88`). Prohibido guardar ViewModels o estado de negocio en `remember { }`.
6. **`rememberSaveable` en forms:** todo estado de formulario (nombre, precios, talles, colores, diálogos) usa `rememberSaveable` para sobrevivir rotación y muerte de proceso. `remember` solo para estado puramente visual/efímero.
7. **Orden de generación:** primero modelos (`domain/model/`), luego interfaces de repositorio (`domain/repository/`), luego implementaciones (`data/repository/`), finalmente pantallas (`presentation/`). No avanzar de fase hasta finalizar la actual.
8. **No modificar código existente sin justificación.** Generar código modular, escalable y preparado para futuras categorías de productos.
9. **Consistencia:** mismos nombres de carpetas y patrones (`XxxScreen`, `XxxViewModel`, `XxxRepository`, `XxxRepositoryImpl`). Documentar decisiones en el PR o en `MEJORAS_APLICADAS.md`.
10. **Firestore tipado:** usar `toObjects(Model::class.java)` + `callbackFlow` + `awaitClose { subscription.remove() }` para listeners en tiempo real (patrón de `ProductRepositoryImpl.kt`).
