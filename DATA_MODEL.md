# Data Model — Stockify (sincronizado con `domain/model/*.kt`)

Fuentes: `ProductDetail.kt`, `ProductSize.kt`, `Category.kt`, `InventoryMovement.kt`,
`SizeRepositoryImpl.kt`, `ProductRepositoryImpl.kt`, `InventoryRepositoryImpl.kt`.

## ProductDetail (`domain/model/ProductDetail.kt:5-21`)

```kotlin
data class ProductDetail(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoriaId: String = "",
    val subcategoriaId: String = "",
    val marca: String = "",
    val precioCosto: Double = 0.0,
    val precioVenta: Double = 0.0,
    val codigoInterno: String = "",
    val codigoBarras: String = "",
    val imagenUrl: String = "",          // URL pública ImgBB (subida vía ImageUploadService, ver README)
    val estado: String = "disponible",   // disponible | agotado | descontinuado
    val createdAt: Long = Date().time,   // orden descendente en getProducts()
    val stock: Int = 0,                  // agregado (los talles llevan su propio stock)
    val colores: List<String> = emptyList()
)
```

## ProductSize (`domain/model/ProductSize.kt:7-12`)

```kotlin
data class ProductSize(
    val id: Int,              // 1..6 según SizeTable (sin default: requerido)
    val name: String,         // requerido (ej. "M")
    val stock: Int = 0,
    val minStock: Int = 0
)

data class ProductWithSizes(
    val productId: String,
    val sizes: List<ProductSize> = emptyList()
)
```

### Tabla de talles (`SizeTable.defaultSizes`, `ProductSize.kt:19-28`)

| id | name |
|----|------|
| 1 | XS |
| 2 | S |
| 3 | M |
| 4 | L |
| 5 | XL |
| 6 | XXL |

`initializeDefaultSizes(productId)` crea los 6 docs en batch; `saveProductSizes` los persiste en batch; lectura ordenada por `id`.

## Category (`domain/model/Category.kt:3-13`)

```kotlin
data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val subcategories: List<Subcategory> = emptyList()
)

data class Subcategory(
    val id: String = "",
    val name: String = ""
)
```

## InventoryMovement (`domain/model/InventoryMovement.kt:9-20`)

```kotlin
enum class MovementType { ENTRADA, SALIDA, AJUSTE }

data class InventoryMovement(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val sizeId: Int = 0,
    val sizeName: String = "",
    val quantity: Int = 0,
    val type: MovementType = MovementType.ENTRADA,
    val reason: String = "",
    val timestamp: Long = Date().time,
    val userId: String = ""
)
```

## Rutas Firestore (multi-tenant `stores/{storeId}`)

```
stores/{storeId}/products/{productId}          # ProductDetail (ProductRepositoryImpl.kt:17-20)
stores/{storeId}/products/{productId}/sizes/{sizeId}  # ProductSize, doc id = size.id.toString() (SizeRepositoryImpl.kt:22-24,39-42,52-54,66-68)
stores/{storeId}/categories/{categoryId}       # Category (CategoryRepositoryImpl.kt:18)
stores/{storeId}/inventory_history/{movementId}
```

> **Nota:** el historial vive en `stores/{storeId}/inventory_history` (`InventoryRepositoryImpl.kt`). Si ves documentos viejos en la colección raíz `inventory_history`, migrarlos y borrarla.

## Notas de mapeo

- Firestore mapea por `toObjects(Model::class.java)`; `MovementType` se serializa como String del enum.
- `createdAt`/`timestamp` son `Long` (epoch ms), no `Timestamp`.
- `getProducts()` ordena por `createdAt DESC`; `searchProducts()` usa rango `nombre` con `\uf8ff`.
