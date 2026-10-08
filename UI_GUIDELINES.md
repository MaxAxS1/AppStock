# UI Guidelines — Stockify (verificado con `presentation/ui/theme/Theme.kt`)

## Paleta real (naranja cálido)

Definida una sola vez en `presentation/ui/theme/Theme.kt:14-31`. La UI consume `MaterialTheme.colorScheme`, nunca literales.

| Rol | Token | Valor |
|---|---|---|
| primary | `OrangeRed` | `#FF5200` |
| secondary / light | `OrangeLight` | `#FF7A45` |
| surface cálida / background | `OrangeSurface` | `#FFF0EA` |
| chips / tags | `OrangeChip` | `#FFE0D0` |
| texto principal | `DarkText` | `#1A1A2E` |
| texto secundario | `MediumGray` | `#7B7B93` |
| superficie cards | `LightSurface` / `White` | `#F9F9F9` / `#FFFFFF` |
| success | `SuccessGreen` | `#22C55E` |
| error | `ErrorRed` | `#EF4444` |

Modo oscuro (`DarkColorScheme` en `Theme.kt:52-68`): `background #121212`, `surface #1E1E2E`, `onBackground/onSurface #F0F0F0`, primario naranja intacto. El modo se resuelve en `AppStockTheme` (`Theme.kt:71-89`) desde `SessionManager.isDarkMode` con fallback a `isSystemInDarkTheme()`.

## Regla de consumo (obligatoria)

- ✅ Usar `MaterialTheme.colorScheme.primary / onPrimary / surface / background / error / ...`
- ❌ **Prohibido `Color(0xFF...)` fijo en UI.** Los únicos `Color(0xFF...)` permitidos viven en `Theme.kt` (definición de tokens). Excepción legacy conocida y pendiente de migrar: `ProductDetailScreen.kt:38,40` define `Primary`/`Secondary` locales → reemplazar por `MaterialTheme.colorScheme`.
- Tipografía y formas: las de **Material3** por defecto (`MaterialTheme.typography`, `MaterialTheme.shapes`). No introducir familias custom sin actualizar esta guía + `Theme.kt`.

## Tipografía Material3

Usar roles (`displayLarge/Medium/Small`, `headlineMedium`, `titleLarge/Medium`, `bodyLarge/Medium`, `labelLarge`) en lugar de tamaños hardcodeados. Títulos de pantalla → `titleLarge`; precios/montos destacados → `headlineMedium`; texto secundario → `bodyMedium` con `onSurfaceVariant`.

## Accesibilidad (mínimos exigibles)

- Toda `Image`/`AsyncImage` (Coil) lleva `contentDescription` significativo (o `null` solo si es puramente decorativa).
- Todo elemento clicable no-`Button` declara `role = Role.Button` + `onClickLabel` en su `Modifier.clickable`.
- Área táctil mínima **48dp** (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` o componentes M3 que ya la garantizan: `Button`, `IconButton`, `FilterChip`).
- Contraste: texto sobre naranja primario siempre `onPrimary` (blanco); no poner `MediumGray` sobre `OrangeSurface` en cuerpo < 14sp.
- Formularios (AddProduct, talles, colores): estado en `rememberSaveable`, errores con `supportingText` + `isError`, foco navegable con teclado.

## Componentes

Cards (surface + `LightSurface`), Bottom Navigation, FloatingActionButton, gráficos de reportes, `FilterChip`/chips para talles (`OrangeChip` en light, `DarkChip` en dark) y colores.
