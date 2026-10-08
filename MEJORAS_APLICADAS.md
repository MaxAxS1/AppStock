# Mejoras aplicadas — Auditoría Stockify (2026-10-08)

Revisión completa del proyecto `app_stock/` (Kotlin + Compose + Firebase) y correcciones aplicadas.
Verificado: sin API key hardcodeada en `src`, sin `close(error)` en repos, colecciones bajo `stores/{storeId}`.

## 1. Tabla de fixes (código)

| ID | Severidad | Archivo:línea | Problema | Fix aplicado |
|----|-----------|---------------|----------|--------------|
| SEC-01 | Crítica | `data/service/ImageUploadService.kt:27` | API key ImgBB hardcodeada y commiteada | Lee `BuildConfig.IMGBB_API_KEY` (inyectada desde `local.properties` → `imgbb.api.key`). `InputStream` y `Response` con `use{}`. `isConfigured()` contra BuildConfig |
| SEC-02 | Crítica | `app/build.gradle.kts:10-16,33,51-54` | Sin BuildConfig, SDK 34, sin minify | `buildConfig=true`, `buildConfigField IMGBB_API_KEY`, `compileSdk/targetSdk=35`, `isMinifyEnabled=true`, `isShrinkResources=true` |
| SEC-03 | Crítica | `.gitignore` (nuevo), `local.properties.ejemplo` (nuevo) | Secretos trackeados (`google-services.json`, `local.properties`, `build/`, `*.hprof`) | `.gitignore` Android estándar. **Pendiente manual:** `git rm --cached` + rotar key ImgBB (la key vieja queda en historial) |
| SEC-04 | Alta | `firestore.rules` (nuevo en `app_stock/`) | Sin reglas versionadas | RBAC por `users/{uid}.storeCode == storeId`, subcolecciones heredan. **Pendiente manual:** `firebase deploy --only firestore:rules` |
| SEC-05 | Alta | `app/proguard-rules.pro` | Vacío, release sin ofuscación | Keeps para Firebase/modelos/Navigation/Coil/OkHttp |
| DAT-01 | Crítica | `data/repository/InventoryRepositoryImpl.kt:18-39` | `products` e `inventory_history` raíz (fuga multi-tenant) | Migrado a `stores/{storeId}/products` + `stores/{storeId}/inventory_history`, `limit(100)`, validación `quantity>0` |
| DAT-02 | Crítica | `data/service/AIServiceImpl.kt:15` | Lee `products` global | Migrado a `stores/{storeId}/products` + `limit(100)`, umbrales a `companion object` |
| DAT-03 | Crítica | `data/repository/AuthRepositoryImpl.kt:74-108` | Registro sin compensación (usuario huérfano) | `try/catch` con `firebaseUser.delete()` si falla `users.set` / `stores.set`. Trim + lowercase email |
| DAT-04 | Alta | `domain/repository/SessionManager.kt:21` | `getStoreId()` lanza en `callbackFlow` | Nuevos `getStoreIdOrNull()` y `requireStoreIdResult()`; `getStoreId()` @Deprecated. Repos usan `OrNull` |
| DAT-05 | Alta | `data/repository/SizeRepositoryImpl.kt:38` | `updateSizeStock` sin transacción (lost-update, negativo) | Transacción read+validate+write, rechaza `<0`, ajusta total |
| DAT-06 | Alta | `data/repository/ProductRepositoryImpl.kt:22-55` + `domain/repository/ProductRepository.kt` | `addProduct(): Result<Unit>`, doble `document()`, sin validación, sin `limit/orderBy` | `addProduct(): Result<String>` (devuelve id), `doc.set()` directo, validación nombre/precio/id, `getProducts().limit(100)`, `searchProducts orderBy("nombre").limit(20)` |
| DAT-07 | Media | `Category/Dashboard/ReportRepositoryImpl` | `close(error)` mataba el Flow, sin `limit`, sin validación | Log + `trySend(emptyList())` / no cerrar, `limit(200/200/500)`, validación categorías, `LOW_STOCK_MAX` a constante |
| DAT-08 | Media | `domain/model/ProductSize.kt:8-9` | `id/name` sin defaults → crash `toObjects()` | Defaults `id=0, name=""` |
| UI-01 | Crítica | `presentation/main/MainScreen.kt:89-198` | `remember{ViewModel()}` pierde estado en rotación | Todo a `viewModel()`, una sola `CategoryViewModel` compartida |
| UI-02 | Crítica | `presentation/products/ProductViewModel.kt:170` | Talles asociados por `nombre+createdAt` (huérfanos) | Usa id devuelto por `addProduct()`. `deleteProduct` solo marca éxito si `isSuccess`, nuevo `deleteError` |
| UI-03 | Crítica | `presentation/inventory/InventoryScreen.kt:56-67` | `MovementItem` mostraba `" - Talle "` y `""` sin cantidad | Muestra `productName + sizeName` y `quantity` con signo, colores `MaterialTheme`, `key={id}`, empty state |
| UI-04 | Crítica | `presentation/sizes/SizeComponents.kt:57` | `Text("Stock: ")` sin número | `Text("Stock: ${size.stock}")`, `key`, `contentDescription` -/+, error si `stock<=minStock` |
| UI-05 | Alta | `presentation/dashboard/DashboardScreen.kt:61` | `BackHandler` bloqueaba todo back | `BackHandler(enabled=isStartDestination)`, loading real, confirmación en `onDelete` |
| UI-06 | Alta | `presentation/categories/CategoryProductsScreen.kt:269` | Diálogo fingía borrado | Llama a `productViewModel.deleteProduct(id)` |
| UI-07 | Alta | `presentation/auth/LoginScreen.kt`, `RegisterScreen.kt`, `AuthViewModel.kt` | `remember` (se pierde en rotación), validación en UI | `rememberSaveable`, `KeyboardOptions` email/password, `validateRegister()` en VM |
| UI-08 | Alta | `presentation/ui/theme/Theme.kt:74` | `collectAsState()` sin lifecycle | `collectAsStateWithLifecycle()` (+ dep `lifecycle-runtime-compose:2.7.0`) |
| UI-09 | Alta | `presentation/inventory/InventoryViewModel.kt`, `presentation/sizes/SizeViewModel.kt` | `ctor(productId)` sin Factory (crash), loading eterno | `Factory(productId)`, `_isLoading=false` en finally |
| DEP-01 | Alta | `app/build.gradle.kts:64-75` | Deps 2023: BOM `2023.08.00`, core-ktx `1.12.0`, etc. | BOM `2024.04.00`, core-ktx `1.13.1`, lifecycle `2.7.0`, activity `1.9.0`, navigation `2.7.7`, coil `2.6.0` |
| DOC-01..04 | Media | `GEMINI_RULES.md`, `UI_GUIDELINES.md`, `DATA_MODEL.md`, `README.md` | Docs desincronizados (Flutter, paleta azul, modelos viejos, ruta clone) | Reescritos al stack/modelos/rutas reales + sección ImgBB/BuildConfig + `firestore.rules` deploy |

## 2. Pendiente manual (no automatizable desde código)

1. Rotar key ImgBB (la vieja `8b73…` queda en historial git) + `imgbb.api.key=...` en tu `local.properties`.
2. `git rm -r --cached app_stock/app/google-services.json app_stock/local.properties app_stock/.gradle app_stock/app/build "*.hprof"` + commit + push. Para purgar historial: `git filter-repo` / BFG (solo 2 commits, fácil).
3. `firebase deploy --only firestore:rules -P <tu-proyecto>` y verificar en consola que la DB no está en modo test abierto.
4. `./gradlew :app:assembleDebug` y luego `assembleRelease` (R8 ahora activo) + test en dispositivo con rotación, sin sesión, y stock concurrente.

## 3. Resumen por apartado: mejora de diseño y funcionalidad

### Auth (Login/Register/AuthViewModel)
- Diseño: `rememberSaveable` (ya no se borra el form al rotar), teclado email/password,alineado a Material3. Pendiente: toggle ver-contraseña, `KeyboardActions.Done`, mensajes de error por campo.
- Funcionalidad: validación centralizada en VM, registro con compensación anti-huérfanos, email normalizado. Pendiente: username único (query previa), verificación de email, recuperación de contraseña real (hoy `Settings` la simula).

### Dashboard
- Diseño: loading real con spinner (antes mostraba 0s), `BackHandler` solo en destino inicial, colores por `colorScheme`. Pendiente: `collectAsStateWithLifecycle` en restantes, `NumberFormat` para moneda, empty state.
- Funcionalidad: query con `limit(200)`, umbral bajo-stock a constante, sin crash sin sesión. Pendiente: agregación `count()` en servidor en vez de traer todo, pull-to-refresh + `retry()`.

### Productos (lista/detalle/alta)
- Diseño: confirmación de borrado consistente (lista pedía/no pedía según origen — ahora siempre confirma), `key` en listas, accesibilidad en thumbnails. Pendiente: unificar `ProductDetailScreen` (colores fijos) con `AddProductScreen` (MaterialTheme), `rememberSaveable` en edición, validación negativos/categoría requerida.
- Funcionalidad: `addProduct` devuelve id (talles ya no huérfanos), `delete` con error expuesto, búsqueda con `orderBy+limit(20)`. Pendiente: `getProductById()` (hoy `loadProductById` trae toda la colección), paginación `startAfter`, borrado en cascada de subcolección `sizes`.

### Categorías
- Diseño: una sola VM compartida (antes dos instancias recargaban), `getCategoryStyle` con caché. Pendiente: botón borrar visible (hoy `onDelete` nunca se invoca), `role=Button` en cards, eliminar código muerto `pressed/scale`.
- Funcionalidad: validación nombre, parse manual de subcategorías (límite Firestore), `limit(200)`. Pendiente: CRUD completo de subcategorías (hoy solo seeder + dropdown), filtro en servidor en vez de `filter{}` en cliente.

### Talles (Sizes)
- Diseño: muestra número de stock (antes vacío), umbral `minStock` consistente con Productos (`<=5`), botones con descripción TalkBack. Pendiente: talles por defecto configurables por categoría (hoy fijos XS-XXL).
- Funcionalidad: stock con transacción (sin lost-update/negativos), Factory para `productId`, loading correcto. Pendiente: `FieldValue.increment` + rules de servidor como doble barrera.

### Inventario (movimientos)
- Diseño: item muestra producto/talle/cantidad/fecha (antes solo signo), empty state, un solo `DateFormat`. Pendiente: pantalla hoy sin ruta en `NavHost` (código muerto) — enlazarla desde detalle producto.
- Funcionalidad: multi-tenant + `limit(100)` + índice `productId+timestamp`, `AJUSTE` aún tratado como entrada — definir signo por tipo. Pendiente: paginación y filtro por rango de fechas.

### Reportes
- Diseño: cálculos fuera de composición (antes `sumOf/maxOf` por recomposición), jank de animaciones por fila. Pendiente: `UiState` sellado Loading/Error/Empty (hoy spinner infinito si falla), `rememberSaveable` en tab, formato moneda único.
- Funcionalidad: `limit(500)`, sin cierre de Flow ante error. Pendiente: reportes que pide `REQUIREMENTS.md` (más vendidos, valor por categoría) hoy divergen; `dailyProfits` agrupa por creación, no por ventas reales — requiere historial de ventas.

### IA / Asistente
- Diseño: `recommendations` se colectaba pero nunca se mostraba (datos muertos). Pendiente: renderizar `RecommendationCard`, auto-scroll al último mensaje, deshabilitar enviar con input blank, `KeyboardActions.Send`.
- Funcionalidad: scope por tienda + `limit`, constantes de umbrales. **No es IA real** (heurística local + `delay(2000)` + keyword matching en VM). Pendiente: mover lógica al repo/caso de uso, usar historial real, o integrar modelo remoto; rate-limit de input.

### Perfil / Empleados
- Diseño: colores fijos rompen dark-mode, `remember(user)` resetea lo tipeado en cada emisión. Pendiente: migrar a `colorScheme`, editar con copia local + Guardar/Cancelar.
- Funcionalidad: listener con leak (sin `remove()` en `onCleared`), `catch{}` que traga errores, sin estados loading/error/success. Pendiente: `callbackFlow` + `awaitClose`, recarga de empleados al cambiar rol.

### Ajustes
- Diseño: versión hardcode `v1.0.0`, `Switch` con track fijo. Pendiente: leer `BuildConfig.VERSION_NAME`, persistir `lowStockAlerts/emailNotifications` en DataStore (hoy `remember` — se pierden).
- Funcionalidad: Exportar/Sincronizar/CambiarPass son `snackbar("simulada")`. Pendiente: implementar o marcar como “Próximamente” para no entregar deuda como real. `setDarkMode` directo desde UI — pasar por VM.

### Transversal
- Diseño: prohibido `Color(0xFF...)` / `10.sp` / `contentDescription=null` / `clickable` sin `role`. Quedan restos en `ProductDetail/Profile/Inventory/Reports` — barrido pendiente.
- Funcionalidad: `collectAsStateWithLifecycle` solo en Theme — extender a todas las screens; crear `UiState` sellado por VM (hoy 4-7 `StateFlow` sueltos → recomposiciones N× y estados inconsistentes); añadir `src/test` + workflow CI (`assembleDebug + test + lint`) — deps declaradas pero jamás ejecutadas.
